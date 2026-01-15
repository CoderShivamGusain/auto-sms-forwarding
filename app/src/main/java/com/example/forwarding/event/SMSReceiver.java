/*
 * This file is part of Android SMS Auto Forwarding Service.
 *
 * Original project licensed under GNU GPL v2
 *
 * Modifications:
 * - Automated background execution
 * - Keyword and sender-based filtering
 * - Blacklist support
 *
 * Modified by < CoderShivamGusain >, 2026
 */

package com.example.forwarding.event;

import com.example.forwarding.data_model.Preferences;
import com.example.forwarding.data_model.RecipientListItem;
import android.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SMSReceiver extends BroadcastReceiver {

    private static final String TAG = "SMSReceiver";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("DEBUG_SMS", "SMS RECEIVED TRIGGERED");
        if (!SMS_RECEIVED.equals(intent.getAction())) return;

        Bundle extras = intent.getExtras();
        if (extras == null) return;

        ArrayList<RecipientListItem> rules =
                Preferences.getRecipientListItems(context);

        if (rules == null || rules.isEmpty()) return;

        SmsMessage[] messages = getSmsMessages(extras);
        if (messages.length == 0) return;

        // ---------------------------------------------------------------------
        // Merge multipart SMS
        // ---------------------------------------------------------------------
        String sender = null;
        StringBuilder bodyBuilder = new StringBuilder();

        for (SmsMessage msg : messages) {
            if (msg == null) continue;
            if (sender == null) sender = msg.getOriginatingAddress();
            bodyBuilder.append(msg.getMessageBody());
        }

        if (sender == null) return;

        sender = sender.trim();
        String body = bodyBuilder.toString().trim();
        if (body.isEmpty()) return;

        String bodyLower = body.toLowerCase(Locale.US);
        String senderLower = sender.toLowerCase(Locale.US);

        // ---------------------------------------------------------------------
        // Evaluate rules
        // ---------------------------------------------------------------------
        Set<String> finalRecipients = new HashSet<>();

        for (RecipientListItem rule : rules) {

            /* -------------------------------------------------------------
             * 1. BLACKLIST (HIGHEST PRIORITY)
             * ------------------------------------------------------------- */
            if (rule.blacklist != null && !rule.blacklist.trim().isEmpty()) {
                String[] bl = rule.blacklist.split(",");
                boolean blocked = false;

                for (String b : bl) {
                    if (!b.trim().isEmpty() &&
                            senderLower.contains(b.trim().toLowerCase(Locale.US))) {
                        blocked = true;
                        break;
                    }
                }

                if (blocked) {
                    continue; // do NOT forward for this rule
                }
            }

            /* -------------------------------------------------------------
             * 2. SENDER WHITELIST
             * ------------------------------------------------------------- */
            ArrayList<RecipientListItem> singleRule = new ArrayList<>();
            singleRule.add(rule);

            ArrayList<String> matchedRecipients =
                    RecipientListItem.match(singleRule, sender,body);

            if (matchedRecipients.isEmpty()) continue;

            /* -------------------------------------------------------------
             * 3. KEYWORD FILTER
             * ------------------------------------------------------------- */
            boolean keywordMatched = false;

            if (rule.keywords == null || rule.keywords.trim().isEmpty()) {
                // No keywords = allow all messages
                keywordMatched = true;
            } else {
                String[] kws = rule.keywords.split(",");
                for (String kw : kws) {
                    if (!kw.trim().isEmpty() &&
                            bodyLower.contains(kw.trim().toLowerCase(Locale.US))) {
                        keywordMatched = true;
                        break;
                    }
                }
            }

            if (!keywordMatched) continue;

            /* -------------------------------------------------------------
             * 4. ADD RECIPIENTS
             * ------------------------------------------------------------- */
            finalRecipients.addAll(matchedRecipients);
        }

        if (finalRecipients.isEmpty()) return;

        // ---------------------------------------------------------------------
        // Forward SMS
        // ---------------------------------------------------------------------
        Log.i(TAG, "Forwarding SMS\nFrom: " + sender + "\nMessage: " + body);

//        String senderContactName =
//                Contacts.getContactName(context, sender);

        String senderContactName = null;

        SMSSender.forward(
                context,
                new ArrayList<>(finalRecipients),
                sender,
                senderContactName,
                body
        );
    }

    // -------------------------------------------------------------------------
    // PDU Parsing
    // -------------------------------------------------------------------------
    private static SmsMessage[] getSmsMessages(Bundle extras) {

        Object[] pdus = (Object[]) extras.get("pdus");
        if (pdus == null || pdus.length == 0)
            return new SmsMessage[0];

        String format = extras.getString("format", "3gpp");
        SmsMessage[] messages = new SmsMessage[pdus.length];

        for (int i = 0; i < pdus.length; i++) {
            try {
                messages[i] =
                        (Build.VERSION.SDK_INT >= 23)
                                ? SmsMessage.createFromPdu((byte[]) pdus[i], format)
                                : SmsMessage.createFromPdu((byte[]) pdus[i]);
            } catch (Exception e) {
                messages[i] = null;
            }
        }
        return messages;
    }
}

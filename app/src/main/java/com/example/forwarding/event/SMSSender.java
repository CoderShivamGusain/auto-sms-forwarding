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

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;

import com.example.forwarding.R;

import java.util.ArrayList;

public final class SMSSender {

    private static final String TAG = "DEBUG_SMS";

    public static void forward(
            Context context,
            ArrayList<String> recipients,
            String sender,
            String senderContactName,
            String body
    ) {
        Log.e(TAG, "➡ SMSSender.forward() called");
        Log.e(TAG, "Sender: " + sender);
        Log.e(TAG, "Recipients count: " + (recipients == null ? 0 : recipients.size()));

        if (recipients == null || recipients.isEmpty()) {
            Log.e(TAG, "❌ No recipients. Aborting.");
            return;
        }

        SmsManager sms = SmsManager.getDefault();

        String preface = context.getString(R.string.sms_preface_heading);

        if (senderContactName != null && !senderContactName.isEmpty()) {
            preface += "\n" + senderContactName;
        }

        preface += "\n" + sender;

        String finalMessage = preface + "\n\n" + body;
        Log.e(TAG, "Final message:\n" + finalMessage);

        ArrayList<String> parts = sms.divideMessage(finalMessage);
        Log.e(TAG, "Message parts: " + parts.size());

        for (String recipient : recipients) {
            try {
                Log.e(TAG, "📤 Sending to: " + recipient);
                sms.sendMultipartTextMessage(recipient, null, parts, null, null);
                Log.e(TAG, "✅ Sent to: " + recipient);
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed sending to: " + recipient, e);
            }
        }
    }
}

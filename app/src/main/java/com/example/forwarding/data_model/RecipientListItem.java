package com.example.forwarding.data_model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

public final class RecipientListItem {

    // Stored as raw strings (comma-separated)
    public String recipient;   // recipients to forward to
    public String sender;      // sender whitelist (* or comma-separated)
    public String keywords;    // message body keywords (*, empty, or comma-separated)
    public String blacklist;   // sender blacklist
    private static final String TAG = "DEBUG_SMS";

    public RecipientListItem() {
        this("", "*", "", "");
    }

    public RecipientListItem(String recipient, String sender, String keywords, String blacklist) {
        this.recipient = recipient;
        this.sender = sender;
        this.keywords = keywords;
        this.blacklist = blacklist;
    }

    @Override
    public String toString() {
        return recipient;
    }

    // ---------------------------------------------------------------------------------------------
    // JSON helpers
    // ---------------------------------------------------------------------------------------------

    public static ArrayList<RecipientListItem> fromJson(String json) {
        return new Gson().fromJson(
                json,
                new TypeToken<ArrayList<RecipientListItem>>() {}.getType()
        );
    }

    public static String toJson(ArrayList<RecipientListItem> arrayList) {
        return new Gson().toJson(arrayList);
    }

    // ---------------------------------------------------------------------------------------------
    // Match recipients (sender + keywords)
    // ---------------------------------------------------------------------------------------------

    public static ArrayList<String> match(
            ArrayList<RecipientListItem> items,
            String sender,
            String message
    ) {
        ArrayList<String> allRecipients = new ArrayList<>();

        for (RecipientListItem item : items) {
            RecipientListItemCriteria criteria =
                    new RecipientListItemCriteria(item);

            criteria.match(sender, message, allRecipients);
        }

        // Remove duplicates
        HashSet<String> unique = new HashSet<>(allRecipients);

        // Never forward back to sender
        if (sender != null) {
            unique.remove(sender.trim());
        }

        return new ArrayList<>(unique);
    }

    // ---------------------------------------------------------------------------------------------
    // Inner class: applies all rules
    // ---------------------------------------------------------------------------------------------

    private static class RecipientListItemCriteria {

        private final String[] recipients;
        private final String[] senderWhitelist;
        private final String[] senderBlacklist;
        private final String[] keywordList;

        RecipientListItemCriteria(RecipientListItem item) {
            recipients = split(item.recipient);
            senderWhitelist = split(item.sender);
            senderBlacklist = split(item.blacklist);
            keywordList = split(item.keywords);
        }

        private static String[] split(String value) {
            if (value == null || value.trim().isEmpty()) {
                return new String[0];
            }
            return value.split("\\s*,\\s*");
        }

        private static String normalize(String s) {
            return s == null ? "" : s.trim().toLowerCase();
        }

        // ----------------------------------------
        // Core matching logic
        // ----------------------------------------
        void match(String sender, String message, ArrayList<String> allRecipients) {

            String normSender = normalize(sender);
            String normMessage = normalize(message);

            // 1️⃣ BLACKLIST (highest priority)
            for (String bl : senderBlacklist) {
                if (!bl.isEmpty() && normSender.contains(normalize(bl))) {
                    Log.d(TAG, "BLOCKED by blacklist: " + bl);
                    return; // BLOCK completely
                }
            }


            // 2️⃣ SENDER WHITELIST
            boolean senderMatched = false;

            if (senderWhitelist.length == 0) {
                senderMatched = true; // backward compatibility
            } else {
                for (String wl : senderWhitelist) {
                    wl = normalize(wl);
                    if (wl.equals("*") || normSender.contains(wl)) {
                        senderMatched = true;
                        break;
                    }
                }
            }
            // 2️⃣ WHITELIST
            if (!senderMatched) {
                Log.d(TAG, "Sender NOT matched: " + sender);
                return;
            }


            // 3️⃣ KEYWORDS
            boolean keywordMatched = false;

            // Empty or "*" = match ALL (this was your missing logic)
            if (keywordList.length == 0) {
                keywordMatched = true;
            } else {
                for (String kw : keywordList) {
                    kw = normalize(kw);
                    if (kw.equals("*") || normMessage.contains(kw)) {
                        keywordMatched = true;
                        break;
                    }
                }
            }
            if (!keywordMatched) {
                Log.d(TAG, "Keyword NOT matched. Message=" + message);
                return;
            }

            // 4️⃣ ADD RECIPIENTS
            for (String rec : recipients) {
                if (!rec.trim().isEmpty()) {
                    Log.d(TAG, "Forwarding to: " + rec);
                    allRecipients.add(rec.trim());
                }
            }

        }
    }
}

package com.johna_sarat_bayanb.fittrack.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

public class MotivationHelper {

    private static final String PREF = "motivation_pref";
    private static final String KEY_LAST_INDEX = "last_index";

    private static final String[] MESSAGES = {
            "No excuses. Just results 💪",
            "Push yourself, you got this!",
            "Small steps lead to big results",
            "Consistency beats motivation",
            "Your future self will thank you",
            "Don’t stop now!",
            "Train insane or remain the same",
            "Make today count",
            "Progress, not perfection",
            "Sweat now, shine later",
            "Stay strong, stay focused",
            "Every rep counts!",
            "One workout closer to your goal",
            "Discipline equals freedom",
            "Be stronger than your excuses",
            "Start now, not tomorrow",
            "Your body can handle it",
            "Earn your results",
            "Stay committed!",
            "You’re doing great!"
    };

    public static String getRandomMessage(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        int lastIndex = prefs.getInt(KEY_LAST_INDEX, -1);

        Random random = new Random();
        int newIndex;

        do {
            newIndex = random.nextInt(MESSAGES.length);
        } while (newIndex == lastIndex);

        prefs.edit().putInt(KEY_LAST_INDEX, newIndex).apply();

        return MESSAGES[newIndex];
    }
}

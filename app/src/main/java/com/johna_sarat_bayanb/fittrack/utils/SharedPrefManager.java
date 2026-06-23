package com.johna_sarat_bayanb.fittrack.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String PREF_NAME = "fittrack_prefs";

    private static final String KEY_GOAL = "daily_goal";
    private static final String KEY_REMINDER = "reminder_enabled";
    private static final String KEY_TIME = "reminder_time";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveGoal(Context context, int minutes) {
        getPrefs(context).edit().putInt(KEY_GOAL, minutes).apply();
    }

    public static int getGoal(Context context) {
        return getPrefs(context).getInt(KEY_GOAL, 30);
    }

    public static void setReminderEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_REMINDER, enabled).apply();
    }

    public static boolean isReminderEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_REMINDER, false);
    }

    public static void saveReminderTime(Context context, String time) {
        getPrefs(context).edit().putString(KEY_TIME, time).apply();
    }

    public static String getReminderTime(Context context) {
        return getPrefs(context).getString(KEY_TIME, "08:00");
    }
}

package com.cmpt.memogram.classes;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    private static final String PREFS_NAME = "app_prefs";
    private static final String VIEWED_POSTS_KEY = "viewed_posts";
    private static final String LAST_LAUNCH_TIMESTAMP_KEY = "last_launch_timestamp";

    public static void setPostViewed(Context context, String postId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(VIEWED_POSTS_KEY + postId, true);
        editor.apply();
    }

    public static boolean isPostViewed(Context context, String postId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(VIEWED_POSTS_KEY + postId, false);
    }

    public static void saveLastLaunchTimestamp(Context context, long timestamp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(LAST_LAUNCH_TIMESTAMP_KEY, timestamp);
        editor.apply();
    }

    public static long getLastLaunchTimestamp(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(LAST_LAUNCH_TIMESTAMP_KEY, 0);
    }
}
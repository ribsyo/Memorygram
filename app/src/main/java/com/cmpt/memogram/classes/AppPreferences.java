package com.cmpt.memogram.classes;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Set;
import java.util.HashSet;

public class AppPreferences {

    private static final String PREFS_NAME = "app_prefs";
    private static final String VIEWED_POSTS_KEY = "viewed_posts_";
    private static final String LAST_LAUNCH_TIMESTAMP_KEY = "last_launch_timestamp";

    public static void setPostViewed(Context context, String postId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> viewedPosts = prefs.getStringSet(VIEWED_POSTS_KEY, new HashSet<>());
        viewedPosts.add(postId);
        editor.putStringSet(VIEWED_POSTS_KEY, viewedPosts);
        editor.apply();
    }

    public static boolean isPostViewed(Context context, String postId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> viewedPosts = prefs.getStringSet(VIEWED_POSTS_KEY, new HashSet<>());
        return viewedPosts.contains(postId);
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

    public static Set<String> getViewedPosts(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(VIEWED_POSTS_KEY, new HashSet<>());
    }

    public static void printViewedPosts(Context context) {
        Set<String> viewedPosts = getViewedPosts(context);
        System.out.println("Viewed Posts: " + viewedPosts);
    }
}
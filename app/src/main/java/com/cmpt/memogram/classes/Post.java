package com.cmpt.memogram.classes;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Post {
    public String title;
    public String text;
    public String audioPath;
    public String audioDownloadLink;
    public String imagePath;
    public String imageDownloadLink;
    public String datePosted;

    public String getRelativeTime() {
        try {
            long postTime;
            if (datePosted.contains("Timestamp")) {
                // Assuming datePosted is in the format "Timestamp(seconds=..., nanoseconds=...)"
                String[] parts = datePosted.split("[=,)]");
                long seconds = Long.parseLong(parts[1].trim());
                postTime = seconds * 1000;
            } else {
                postTime = Long.parseLong(datePosted);
            }

            long currentTime = System.currentTimeMillis();
            long diff = currentTime - postTime;

            long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (seconds < 60) {
                return "just now";
            } else if (minutes == 1) {
                return "a minute ago";
            } else if (minutes > 1 && minutes < 60) {
                return minutes + " minutes ago";
            } else if (hours < 24) {
                return hours + " hours ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
                Date date = new Date(postTime);
                return sdf.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown time";
        }
    }
}
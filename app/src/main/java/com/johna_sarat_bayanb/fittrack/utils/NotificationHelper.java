package com.johna_sarat_bayanb.fittrack.utils;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.johna_sarat_bayanb.fittrack.R;

public class NotificationHelper {

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    WorkoutConstants.CHANNEL_ID,
                    "Workout Service",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static Notification getNotification(Context context, String text) {
        return new NotificationCompat.Builder(context, WorkoutConstants.CHANNEL_ID)
                .setContentTitle("FitTrack Running")
                .setContentText(text)
                .setSmallIcon(R.drawable.fit_track)
                .setOngoing(true)
                .build();
    }
}

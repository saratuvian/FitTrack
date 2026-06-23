package com.johna_sarat_bayanb.fittrack.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.johna_sarat_bayanb.fittrack.R;
import com.johna_sarat_bayanb.fittrack.utils.AlarmHelper;
import com.johna_sarat_bayanb.fittrack.utils.MotivationHelper;
import com.johna_sarat_bayanb.fittrack.utils.SharedPrefManager;
import com.johna_sarat_bayanb.fittrack.utils.WorkoutConstants;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int goal = SharedPrefManager.getGoal(context);

        String motivation = MotivationHelper.getRandomMessage(context);

        String message = motivation + "\nGoal: " + goal + " minutes";

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, WorkoutConstants.CHANNEL_ID)
                        .setSmallIcon(R.drawable.fit_track)
                        .setContentTitle("FitTrack Reminder")
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.notify(1001, builder.build());
        }

        AlarmHelper.scheduleReminder(
                context,
                SharedPrefManager.getReminderTime(context)
        );

    }
}
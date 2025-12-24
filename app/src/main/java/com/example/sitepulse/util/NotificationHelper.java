package com.example.sitepulse.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.example.sitepulse.R;

public class NotificationHelper {

    public static final String CHANNEL_ID_GENERAL = "general_channel";
    public static final String CHANNEL_ID_UPDATES = "updates_channel";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                // General Channel
                NotificationChannel channelGeneral = new NotificationChannel(
                        CHANNEL_ID_GENERAL,
                        "General Notifications",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                channelGeneral.setDescription("General app notifications");

                // Updates Channel (High Importance)
                NotificationChannel channelUpdates = new NotificationChannel(
                        CHANNEL_ID_UPDATES,
                        "Project Updates",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channelUpdates.setDescription("Important project updates and approvals");

                notificationManager.createNotificationChannel(channelGeneral);
                notificationManager.createNotificationChannel(channelUpdates);
            }
        }
    }
}

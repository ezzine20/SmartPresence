package com.example.emsi_smartpresence.notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.example.emsi_smartpresence.R;

public class EventReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "event_reminder_channel";
    private static final int NOTIFICATION_ID = 100; // Unique ID for event notifications

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: Extract event details from intent extras
        String eventTitle = intent.getStringExtra("eventTitle");
        String eventLocation = intent.getStringExtra("eventLocation");

        // Create and show the notification
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(eventTitle != null ? eventTitle : "Rappel d'événement")
                .setContentText(eventLocation != null && !eventLocation.isEmpty() ? 
                        "Lieu : " + eventLocation : "")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // TODO: Add pending intent to open the event details when clicked

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
} 
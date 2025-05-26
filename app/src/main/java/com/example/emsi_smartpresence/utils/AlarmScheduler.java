package com.example.emsi_smartpresence.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.emsi_smartpresence.notifications.EventReminderReceiver;

public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";
    private static final String EVENT_REMINDER_ACTION = "com.example.emsi_smartpresence.EVENT_REMINDER";

    public static void scheduleEventAlarm(Context context, String eventId, String title, String location, long startTimeMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, EventReminderReceiver.class);
        intent.setAction(EVENT_REMINDER_ACTION);
        intent.putExtra("eventTitle", title);
        intent.putExtra("eventLocation", location);

        // Use a unique request code for each notification
        int requestCode = eventId.hashCode(); // Consider a more robust method for large number of events

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        if (startTimeMillis < System.currentTimeMillis()) {
            Log.d(TAG, "Event time is in the past, not scheduling alarm.");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTimeMillis, pendingIntent);
                Log.d(TAG, "Scheduled exact alarm for event: " + eventId);
            } else {
                Log.w(TAG, "Permission to schedule exact alarms not granted.");
                // Handle the case where the permission is not granted (e.g., show a message to the user)
                // Toast.makeText(context, "Permission to schedule exact alarms not granted", Toast.LENGTH_LONG).show();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTimeMillis, pendingIntent);
            Log.d(TAG, "Scheduled exact and allow while idle alarm for event: " + eventId);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, startTimeMillis, pendingIntent);
            Log.d(TAG, "Scheduled exact alarm for event: " + eventId);
        }
    }

    public static void cancelEventAlarm(Context context, String eventId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, EventReminderReceiver.class);
        intent.setAction(EVENT_REMINDER_ACTION);

        int requestCode = eventId.hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Canceled alarm for event: " + eventId);
    }
} 
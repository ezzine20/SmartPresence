package com.example.emsi_smartpresence.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.emsi_smartpresence.utils.AlarmScheduler;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, rescheduling alarms");
            // Reschedule all future event alarms
            rescheduleEventAlarms(context);
        }
    }

    private void rescheduleEventAlarms(Context context) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No user logged in, cannot reschedule alarms");
            return;
        }

        long currentTimeMillis = Calendar.getInstance().getTimeInMillis();

        db.collection("events")
                .whereEqualTo("userId", currentUser.getUid())
                .whereGreaterThanOrEqualTo("startTime", currentTimeMillis)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventId = document.getId();
                            String title = document.getString("title");
                            String location = document.getString("location");
                            Long startTime = document.getLong("startTime");

                            if (startTime != null) {
                                // Reschedule the alarm for this event
                                // We need to call the scheduling logic again
                                // For now, let's log it. We'll create a helper function next.
                                Log.d(TAG, "Need to reschedule alarm for event: " + title + " at " + startTime);
                                // TODO: Call helper function to schedule alarm
                                AlarmScheduler.scheduleEventAlarm(
                                    context,
                                    eventId,
                                    title,
                                    location,
                                    startTime
                                );
                            }
                        }
                    } else {
                        Log.e(TAG, "Error fetching events for rescheduling: ", task.getException());
                    }
                });
    }
} 
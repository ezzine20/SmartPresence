package com.example.emsi_smartpresence;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.example.emsi_smartpresence.utils.AlarmScheduler;

public class NewEventActivity extends AppCompatActivity {

    private static final String TAG = "NewEventActivity";

    private TextInputEditText eventTitleInput;
    private TextInputEditText eventLocationInput;
    private MaterialSwitch allDaySwitch;
    private MaterialButton startDateButton, startTimeButton, endDateButton, endTimeButton;
    private LinearLayout endDateLayout;

    private Calendar startDateTime, endDateTime;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vous devez être connecté pour créer un événement.", Toast.LENGTH_LONG).show();
            finish(); // Ferme l'activité si l'utilisateur n'est pas connecté
            return;
        }

        setContentView(R.layout.activity_new_event);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nouvel événement");
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        eventTitleInput = findViewById(R.id.eventTitleInput);
        eventLocationInput = findViewById(R.id.eventLocationInput);
        allDaySwitch = findViewById(R.id.allDaySwitch);
        startDateButton = findViewById(R.id.startDateButton);
        startTimeButton = findViewById(R.id.startTimeButton);
        endDateButton = findViewById(R.id.endDateButton);
        endTimeButton = findViewById(R.id.endTimeButton);
        endDateLayout = findViewById(R.id.endDateLayout);

        // Initialize Calendar instances
        startDateTime = Calendar.getInstance();
        endDateTime = Calendar.getInstance();
        endDateTime.add(Calendar.HOUR_OF_DAY, 1); // Default end time is 1 hour after start

        // Update button text initially
        updateDateButtonText(startDateButton, startDateTime);
        updateTimeButtonText(startTimeButton, startDateTime);
        updateDateButtonText(endDateButton, endDateTime);
        updateTimeButtonText(endTimeButton, endDateTime);

        // Set click listeners for date and time buttons
        startDateButton.setOnClickListener(v -> showDatePicker(startDateTime, startDateButton, startTimeButton));
        startTimeButton.setOnClickListener(v -> showTimePicker(startDateTime, startTimeButton));
        endDateButton.setOnClickListener(v -> showDatePicker(endDateTime, endDateButton, endTimeButton));
        endTimeButton.setOnClickListener(v -> showTimePicker(endDateTime, endTimeButton));

        // Implement allDaySwitch listener
        allDaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // All day event, hide end time
                endDateLayout.setVisibility(View.GONE);
                startTimeButton.setVisibility(View.GONE);
                endTimeButton.setVisibility(View.GONE);
            } else {
                // Not all day, show end time
                endDateLayout.setVisibility(View.VISIBLE);
                startTimeButton.setVisibility(View.VISIBLE);
                endTimeButton.setVisibility(View.VISIBLE);
            }
        });

        // Set initial state of end date/time based on switch
        allDaySwitch.setChecked(false); // Default to not all day
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle "Annuler" button press (back arrow)
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            saveEvent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDatePicker(Calendar calendar, MaterialButton dateButton, MaterialButton timeButton) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    updateDateButtonText(dateButton, calendar);
                    // If start date is changed and is after end date, update end date
                    if (calendar == startDateTime && startDateTime.after(endDateTime)) {
                        endDateTime.setTimeInMillis(startDateTime.getTimeInMillis());
                        endDateTime.add(Calendar.HOUR_OF_DAY, 1);
                        updateDateButtonText(endDateButton, endDateTime);
                        updateTimeButtonText(endTimeButton, endDateTime);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker(Calendar calendar, MaterialButton timeButton) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateTimeButtonText(timeButton, calendar);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // 24 hour format
        );
        timePickerDialog.show();
    }

    private void updateDateButtonText(MaterialButton button, Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        button.setText(dateFormat.format(calendar.getTime()));
    }

    private void updateTimeButtonText(MaterialButton button, Calendar calendar) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        button.setText(timeFormat.format(calendar.getTime()));
    }

    private void saveEvent() {
        String title = eventTitleInput.getText().toString().trim();
        String location = eventLocationInput.getText().toString().trim();
        boolean isAllDay = allDaySwitch.isChecked();

        if (title.isEmpty()) {
            Toast.makeText(this, "Le titre de l'événement ne peut pas être vide", Toast.LENGTH_SHORT).show();
            return;
        }

        // You can add more validation here if needed (e.g., start time before end time)

        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("location", location);
        event.put("isAllDay", isAllDay);
        event.put("startTime", startDateTime.getTimeInMillis());
        if (!isAllDay) {
            event.put("endTime", endDateTime.getTimeInMillis());
        }

        // Utiliser l'ID de l'utilisateur connecté
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
             // Cela ne devrait pas arriver si la vérification en onCreate fonctionne, mais c'est une sécurité supplémentaire.
             Log.e(TAG, "User is null in saveEvent. Cannot save event.");
             Toast.makeText(this, "Erreur: utilisateur non connecté.", Toast.LENGTH_LONG).show();
             return;
        }
        event.put("userId", userId);

        db.collection("events")
            .add(event)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(NewEventActivity.this, "Événement ajouté avec succès", Toast.LENGTH_SHORT).show();

                // Schedule notification for the event
                AlarmScheduler.scheduleEventAlarm(
                    NewEventActivity.this,
                    documentReference.getId(),
                    title,
                    location,
                    startDateTime.getTimeInMillis()
                );

                finish(); // Close this activity after saving
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de l'ajout de l'événement: " + e.getMessage(), e);
                Toast.makeText(NewEventActivity.this, "Erreur lors de l'ajout de l'événement: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // Afficher le message d'erreur détaillé
            });
    }
} 
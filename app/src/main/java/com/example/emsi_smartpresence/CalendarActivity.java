package com.example.emsi_smartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.emsi_smartpresence.adapters.EventAdapter;
import com.example.emsi_smartpresence.models.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CalendarView calendarView;
    private RecyclerView eventsRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private static final String TAG = "CalendarActivity";
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Calendrier");
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get CalendarView instance
        calendarView = findViewById(R.id.calendarView);

        // Setup RecyclerView
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList);
        eventsRecyclerView.setAdapter(eventAdapter);

        // Initialize ProgressBar
        progressBar = findViewById(R.id.progressBar);

        // Set listener for date changes
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Fetch events for the selected date
            fetchEventsForDate(year, month, dayOfMonth);
        });

        // Fetch events for the current date initially
        Calendar today = Calendar.getInstance();
        fetchEventsForDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        // Gestion du bouton retour
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // TODO: Implement calendar view and functionality
    }

    private void fetchEventsForDate(int year, int month, int dayOfMonth) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "User not logged in, cannot fetch events.");
            Toast.makeText(this, "Veuillez vous connecter pour voir le calendrier.", Toast.LENGTH_LONG).show();
            eventList.clear(); // Clear previous events
            eventAdapter.notifyDataSetChanged();
            // Consider redirecting to login screen here if needed
            return;
        }

        Calendar startOfDay = Calendar.getInstance();
        startOfDay.set(year, month, dayOfMonth, 0, 0, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = Calendar.getInstance();
        endOfDay.set(year, month, dayOfMonth, 23, 59, 59);
        endOfDay.set(Calendar.MILLISECOND, 999);

        progressBar.setVisibility(ProgressBar.VISIBLE);
        db.collection("events")
            .whereEqualTo("userId", mAuth.getCurrentUser().getUid()) // Activer le filtrage par userId
            .whereGreaterThanOrEqualTo("startTime", startOfDay.getTimeInMillis())
            .whereLessThanOrEqualTo("startTime", endOfDay.getTimeInMillis())
            .limit(50)
            .get()
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(ProgressBar.GONE);
                if (task.isSuccessful()) {
                    eventList.clear(); // Clear previous events
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Event event = document.toObject(Event.class);
                        event.setId(document.getId()); // Set the document ID as event ID
                        // Firestore timestamp is in milliseconds, convert to Date
                        if (document.get("startTime") instanceof Long) {
                            event.setStartTime(document.getLong("startTime"));
                        } else { // Handle Timestamp if necessary, depending on how you save
                            // event.setStartTime(document.getTimestamp("startTime").toDate());
                        }
                        if (!event.isAllDay() && document.get("endTime") instanceof Long) {
                            event.setEndTime(document.getLong("endTime"));
                        } else { // Handle Timestamp if necessary
                            // event.setEndTime(document.getTimestamp("endTime").toDate());
                        }
                        eventList.add(event);
                    }
                    eventAdapter.notifyDataSetChanged();
                } else {
                    Log.w(TAG, "Error fetching events: ", task.getException());
                    Toast.makeText(this, "Erreur lors du chargement des événements", Toast.LENGTH_SHORT).show();
                    eventList.clear(); // Clear list on error
                    eventAdapter.notifyDataSetChanged();
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_calendar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_add_event) {
            // Open NewEventActivity to add a new event
            Intent intent = new Intent(this, NewEventActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 
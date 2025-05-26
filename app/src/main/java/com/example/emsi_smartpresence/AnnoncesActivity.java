package com.example.emsi_smartpresence;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import com.example.emsi_smartpresence.adapters.AnnouncementAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

public class AnnoncesActivity extends AppCompatActivity {
    private static final String TAG = "AnnoncesActivity";

    private RecyclerView announcementsRecyclerView;
    private AnnouncementAdapter announcementAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: AnnoncesActivity created");
        setContentView(R.layout.activity_annonces);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Annonces");
        }

        db = FirebaseFirestore.getInstance();

        announcementsRecyclerView = findViewById(R.id.announcementsRecyclerView);
        announcementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        announcementAdapter = new AnnouncementAdapter(new ArrayList<DocumentSnapshot>());
        announcementsRecyclerView.setAdapter(announcementAdapter);

        fetchAnnouncements();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchAnnouncements() {
        Log.d(TAG, "fetchAnnouncements: Starting to fetch announcements");
        db.collection("announcements")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> announcementsList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "fetchAnnouncements: Found announcement - " + document.getData());
                            announcementsList.add(document);
                        }
                        Log.d(TAG, "fetchAnnouncements: Total announcements found: " + announcementsList.size());
                        announcementAdapter.setAnnouncements(announcementsList);
                    } else {
                        Log.e(TAG, "fetchAnnouncements: Error getting announcements", task.getException());
                        Toast.makeText(AnnoncesActivity.this, 
                            "Erreur lors du chargement des annonces", 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 
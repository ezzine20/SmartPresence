package com.example.emsi_smartpresence;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.emsi_smartpresence.adapters.HistoryAdapter;
// import com.example.emsi_smartpresence.models.HistoryItem; // Using inner class for now

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Date;

public class HistoriqueActivity extends AppCompatActivity {

    private static final String TAG = "HistoriqueActivity";

    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyList;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Historique");
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyList);
        historyRecyclerView.setAdapter(historyAdapter);

        progressBar = findViewById(R.id.progressBar);

        fetchHistory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchHistory() {
        // Vérifier si l'utilisateur est connecté
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, cannot fetch history.");
            Toast.makeText(this, "Veuillez vous connecter pour voir l'historique.", Toast.LENGTH_LONG).show();
            historyList.clear();
            historyAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE); // Masquer la progress bar
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        historyList.clear();

        // Fetch Reclamations with user filter
        db.collection("claims")
            .whereEqualTo("userId", mAuth.getCurrentUser().getUid()) // Ajouter le filtre userId
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        HistoryItem item = new HistoryItem();
                        item.setTitle(document.getString("title"));
                        item.setType("Réclamation");
                        // Use a timestamp that exists in claims, adjust if needed
                        item.setTimestamp(document.getLong("timestamp")); 
                        historyList.add(item);
                    }
                    // Fetch Events after fetching Reclamations
                    fetchEventsForHistory();
                } else {
                    Log.w(TAG, "Error fetching claims: ", task.getException());
                    // Continue to fetch events even if claims fail
                    fetchEventsForHistory();
                    Toast.makeText(this, "Erreur lors du chargement des réclamations", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void fetchEventsForHistory() {
        // Pas besoin de vérifier l'utilisateur ici, car fetchHistory le fait déjà.
        // Utiliser mAuth.getCurrentUser() directement est sûr car la méthode n'est appelée que si l'utilisateur est connecté.
        
        // Fetch Events with user filter
        db.collection("events")
            .whereEqualTo("userId", mAuth.getCurrentUser().getUid()) // Ajouter le filtre userId
            .get()
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        HistoryItem item = new HistoryItem();
                        item.setTitle(document.getString("title"));
                        item.setType("Événement");
                        // Assuming 'startTime' exists and is a Long timestamp
                        item.setTimestamp(document.getLong("startTime")); 
                        historyList.add(item);
                    }
                    // Sort and display the combined list
                    sortAndDisplayHistory();
                } else {
                    Log.w(TAG, "Error fetching events: ", task.getException());
                    Toast.makeText(this, "Erreur lors du chargement des événements", Toast.LENGTH_SHORT).show();
                    sortAndDisplayHistory(); // Display available items even if events fail
                }
            });
    }

    private void sortAndDisplayHistory() {
        // Sort by timestamp in descending order (most recent first)
        Collections.sort(historyList, Comparator.comparingLong(HistoryItem::getTimestamp).reversed());
        historyAdapter.notifyDataSetChanged();
    }

    // Simple model to hold combined history data
    public static class HistoryItem {
        private String title;
        private String type;
        private long timestamp;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
} 
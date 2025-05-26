package com.example.emsi_smartpresence;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class AbsenceActivity extends AppCompatActivity {
    private static final String TAG = "AbsenceActivity";

    private TextInputEditText dateInput;
    private ChipGroup seanceChipGroup;
    private TextInputEditText motifInput;
    private MaterialButton submitButton;
    private RecyclerView absencesRecyclerView;
    private AbsenceAdapter absenceAdapter;
    private List<Absence> absences = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CircularProgressIndicator progressIndicator;
    private View contentLayout;
    private AtomicBoolean isActivityActive = new AtomicBoolean(true);
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisation de Firebase et vérification de l'authentification
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vous devez être connecté pour déclarer une absence.", Toast.LENGTH_LONG).show();
            finish(); // Ferme l'activité si l'utilisateur n'est pas connecté
            return;
        }

        setContentView(R.layout.activity_absence);

        // Initialisation de la barre d'outils
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialisation des vues
        initializeViews();
        
        // Afficher le chargement
        showLoading(true);
        
        // Initialisation de Firebase de manière asynchrone
        initializeFirebase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityActive.set(false);
        executorService.shutdown();
    }

    private void initializeViews() {
        dateInput = findViewById(R.id.dateInput);
        seanceChipGroup = findViewById(R.id.seanceChipGroup);
        motifInput = findViewById(R.id.motifInput);
        submitButton = findViewById(R.id.submitButton);
        absencesRecyclerView = findViewById(R.id.absencesRecyclerView);
        progressIndicator = findViewById(R.id.progressIndicator);
        contentLayout = findViewById(R.id.contentLayout);

        // Configuration du RecyclerView
        absenceAdapter = new AbsenceAdapter(absences);
        absencesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        absencesRecyclerView.setAdapter(absenceAdapter);

        // Désactiver le bouton de soumission initialement
        submitButton.setEnabled(false);

        // Configurer les puces pour être checkables
        for (int i = 0; i < seanceChipGroup.getChildCount(); i++) {
            View child = seanceChipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setCheckable(true);
            }
        }
    }

    private void showLoading(boolean show) {
        if (!isActivityActive.get()) return;
        runOnUiThread(() -> {
            progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
            contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        });
    }

    private void initializeFirebase() {
        new Thread(() -> {
            try {
                db = FirebaseFirestore.getInstance();
                
                if (!isActivityActive.get()) return;
                
                runOnUiThread(() -> {
                    if (!isActivityActive.get()) return;
                    submitButton.setEnabled(true);
                    showLoading(false);
                    setupListeners();
                    loadAbsences();
                });
            } catch (Exception e) {
                if (!isActivityActive.get()) return;
                runOnUiThread(() -> {
                    if (!isActivityActive.get()) return;
                    showLoading(false);
                    Toast.makeText(this, "Erreur de connexion. Veuillez vérifier votre connexion internet.", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void setupListeners() {
        dateInput.setOnClickListener(v -> showDatePicker());
        submitButton.setOnClickListener(v -> submitAbsence());
    }

    private void showDatePicker() {
        if (!isActivityActive.get()) return;
        
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                if (!isActivityActive.get()) return;
                calendar.set(year, month, dayOfMonth);
                dateInput.setText(String.format("%02d/%02d/%d", dayOfMonth, month + 1, year));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void submitAbsence() {
        if (!isActivityActive.get()) return;
        
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Pas de connexion internet. Veuillez réessayer.", Toast.LENGTH_LONG).show();
            return;
        }

        if (dateInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (seanceChipGroup.getCheckedChipIds().isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner au moins une séance", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        List<String> selectedSeances = new ArrayList<>();
        for (int chipId : seanceChipGroup.getCheckedChipIds()) {
            Chip chip = findViewById(chipId);
            selectedSeances.add(chip.getText().toString());
        }

        Map<String, Object> absence = new HashMap<>();
        absence.put("date", dateInput.getText().toString());
        absence.put("seances", selectedSeances);
        absence.put("motif", motifInput.getText().toString());
        absence.put("userId", mAuth.getCurrentUser().getUid());
        absence.put("status", "En attente");
        absence.put("timestamp", System.currentTimeMillis());

        db.collection("absences")
            .add(absence)
            .addOnSuccessListener(documentReference -> {
                if (!isActivityActive.get()) return;
                showLoading(false);
                Toast.makeText(this, "Absence déclarée avec succès", Toast.LENGTH_SHORT).show();
                clearForm();
                loadAbsences();
            })
            .addOnFailureListener(e -> {
                if (!isActivityActive.get()) return;
                showLoading(false);
                Log.e(TAG, "Erreur lors de la déclaration de l'absence", e);
                Toast.makeText(this, "Erreur lors de la déclaration de l'absence: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        android.net.Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }
        android.net.NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null && (capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    private void clearForm() {
        if (!isActivityActive.get()) return;
        dateInput.setText("");
        seanceChipGroup.clearCheck();
        motifInput.setText("");
    }

    private void loadAbsences() {
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, cannot load absences.");
            Toast.makeText(this, "Veuillez vous connecter pour voir vos absences.", Toast.LENGTH_LONG).show();
            absences.clear();
            absenceAdapter.notifyDataSetChanged();
            showLoading(false);
            return;
        }

        showLoading(true);
        
        db.collection("absences")
            .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!isActivityActive.get()) return;
                
                List<Absence> fetchedAbsences = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        @SuppressWarnings("unchecked")
                        List<String> seances = (List<String>) doc.get("seances");
                        String date = doc.getString("date");
                        String motif = doc.getString("motif");
                        String status = doc.getString("status");
                        
                        if (date != null && seances != null) {
                            Absence absence = new Absence(date, seances, motif, status);
                            fetchedAbsences.add(absence);
                        } else {
                            Log.w(TAG, "Données d'absence incomplètes pour le document: " + doc.getId());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de la conversion des données d'absence", e);
                    }
                }

                runOnUiThread(() -> {
                    if (!isActivityActive.get()) return;
                    absences.clear();
                    absences.addAll(fetchedAbsences);
                    absenceAdapter.notifyDataSetChanged();
                    showLoading(false);
                    
                    // Afficher un message si aucune absence n'est trouvée
                    if (fetchedAbsences.isEmpty()) {
                        Toast.makeText(this, "Aucune absence trouvée", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors du chargement des absences", e);
                runOnUiThread(() -> {
                    if (!isActivityActive.get()) return;
                    showLoading(false);
                    Toast.makeText(this, "Erreur lors du chargement des absences: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 
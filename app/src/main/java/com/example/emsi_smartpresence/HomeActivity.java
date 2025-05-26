package com.example.emsi_smartpresence;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.WorkManager;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.emsi_smartpresence.utils.TestDataGenerator;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    private TextView dashboardAdminName;
    private CardView cardFacture, cardAnnonce, cardHistorique, cardReclamations, cardAProximite, cardPlanning, cardDocuments, cardSettings;
    private View notificationIndicator;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ExecutorService executorService;
    private FloatingActionButton fabEinstein;
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private boolean clicked = false;

    private static final String TAG = "HomeActivity";
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_LAST_SEEN_NOTIFICATIONS = "lastSeenNotifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        
        // Initialiser l'ExecutorService pour les opérations en arrière-plan
        executorService = Executors.newFixedThreadPool(3);
        
        // Initialiser Firebase dans un thread séparé
        executorService.execute(() -> {
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            // Réactiver la vérification de connexion
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                runOnUiThread(() -> {
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    startActivity(loginIntent);
                    finish();
                });
                return;
            }

            // Générer les données de test UNIQUEMENT si l'utilisateur est connecté
            TestDataGenerator.generateTestData(currentUser.getUid());

            // Récupérer les informations de l'utilisateur
            db.collection("users")
                .document(currentUser.getUid()) // Utiliser l'ID de l'utilisateur connecté
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String fullName = firstName + " " + lastName;
                        runOnUiThread(() -> {
                            dashboardAdminName.setText(fullName);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la récupération des données utilisateur", e);
                });
        });

        // Initialiser l'interface utilisateur sur le thread principal
        initializeUI();
    }

    private void initializeUI() {
        // La gestion des insets est maintenant gérée par le layout et les comportements
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */

        // Initialisation des vues
        dashboardAdminName = findViewById(R.id.dashboard_adminName);
        cardFacture = findViewById(R.id.card1);
        cardAnnonce = findViewById(R.id.card2);
        cardHistorique = findViewById(R.id.history);
        cardReclamations = findViewById(R.id.card4);
        cardAProximite = findViewById(R.id.card5);
        cardPlanning = findViewById(R.id.card6);
        cardDocuments = findViewById(R.id.cardDocuments);
        notificationIndicator = findViewById(R.id.notificationIndicator);
        cardSettings = findViewById(R.id.cardSettings);

        // Configuration de la barre d'outils
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Accueil");
            // S'assurer que le bouton de navigation est cliquable
            toolbar.setNavigationOnClickListener(v -> {
                startActivity(new Intent(this, SettingsActivity.class));
            });
            toolbar.setClickable(true);
            toolbar.setFocusable(true);
        }

        // Configuration du FAB Einstein
        fabEinstein = findViewById(R.id.fabEinstein);
        fabEinstein.setOnClickListener(v -> {
            onEinsteinButtonClicked();
        });

        // Configuration des listeners de clic
        setupCardListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Vérifier les notifications dans un thread séparé
        executorService.execute(this::checkNewNotifications);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void setupCardListeners() {
        cardFacture.setOnClickListener(v -> {
            // TODO: Naviguer vers l'écran Facture
            Intent intent = new Intent(HomeActivity.this, AbsenceActivity.class);
            startActivity(intent);
        });

        cardAnnonce.setOnClickListener(v -> {
            markNotificationsAsSeen();
            try {
                startActivity(new Intent(HomeActivity.this, AnnoncesActivity.class));
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du lancement de AnnoncesActivity", e);
            }
        });

        cardHistorique.setOnClickListener(v -> {
            try {
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(HomeActivity.this, 
                        "Veuillez vous connecter pour accéder à cette fonctionnalité", 
                        Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(HomeActivity.this, HistoriqueActivity.class));
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du lancement de HistoriqueActivity", e);
                Toast.makeText(HomeActivity.this,
                    "Une erreur est survenue lors du chargement de l'historique",
                    Toast.LENGTH_SHORT).show();
            }
        });

        cardReclamations.setOnClickListener(v -> {
            try {
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(HomeActivity.this, 
                        "Veuillez vous connecter pour accéder à cette fonctionnalité", 
                        Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(HomeActivity.this, ReclamationsActivity.class));
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du lancement de ReclamationsActivity", e);
                Toast.makeText(HomeActivity.this, 
                    "Une erreur est survenue lors du chargement des réclamations", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        cardAProximite.setOnClickListener(v -> {
            try {
                startActivity(new Intent(HomeActivity.this, NearbyActivity.class));
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du lancement de NearbyActivity", e);
            }
        });

        cardPlanning.setOnClickListener(v -> {
            try {
                startActivity(new Intent(HomeActivity.this, CalendarActivity.class));
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du lancement de CalendarActivity", e);
            }
        });

        cardDocuments.setOnClickListener(v -> {
            try {
                startActivity(new Intent(HomeActivity.this, DocumentsActivity.class));
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du lancement de DocumentsActivity", e);
            }
        });

        cardSettings.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        });

        // Ajout du listener pour la carte de discussion
        findViewById(R.id.cardDiscussion).setOnClickListener(v -> {
            try {
                startActivity(new Intent(HomeActivity.this, GroupListActivity.class));
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du lancement de GroupListActivity", e);
                v.postDelayed(() -> {
                    Toast.makeText(HomeActivity.this,
                        "Cette fonctionnalité n'est pas encore disponible",
                        Toast.LENGTH_SHORT).show();
                }, 500);
            }
        });
    }

    private void checkNewNotifications() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            runOnUiThread(() -> notificationIndicator.setVisibility(View.GONE));
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastSeenTimestamp = prefs.getLong(KEY_LAST_SEEN_NOTIFICATIONS, 0);

        db.collection("events")
                .whereEqualTo("userId", currentUser.getUid())
                .whereGreaterThan("startTime", lastSeenTimestamp)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        runOnUiThread(() -> {
                            if (!task.getResult().isEmpty()) {
                                notificationIndicator.setVisibility(View.VISIBLE);
                            } else {
                                notificationIndicator.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        Log.w(TAG, "Error checking for new notifications: ", task.getException());
                        runOnUiThread(() -> notificationIndicator.setVisibility(View.GONE));
                    }
                });
    }

    private void markNotificationsAsSeen() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_LAST_SEEN_NOTIFICATIONS, System.currentTimeMillis());
        editor.apply();
        notificationIndicator.setVisibility(View.GONE);
    }

    private void onEinsteinButtonClicked() {
        // Animation de rebond
        fabEinstein.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(150)
                .withEndAction(() -> {
                    fabEinstein.animate()
                            .scaleX(0.8f)
                            .scaleY(0.8f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                fabEinstein.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100)
                                        .withEndAction(() -> {
                                            // Animation de rotation avec rebond
                                            fabEinstein.animate()
                                                    .rotationBy(360)
                                                    .setDuration(600)
                                                    .setInterpolator(new android.view.animation.BounceInterpolator())
                                                    .withEndAction(() -> {
                                                        // Lancer l'activité de l'assistant virtuel avec une transition fluide
                                                        Intent intent = new Intent(this, AssistantVirtuelActivity.class);
                                                        startActivity(intent);
                                                    })
                                                    .start();
                                        })
                                        .start();
                            })
                            .start();
                })
                .start();
    }
} 
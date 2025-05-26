package com.example.emsi_smartpresence;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ReclamationsActivity extends AppCompatActivity {
    private TextInputEditText titleInput;
    private TextInputEditText descriptionInput;
    private MaterialButton submitButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialisation de Firebase et vérification de l'authentification
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vous devez être connecté pour soumettre une réclamation.", Toast.LENGTH_LONG).show();
            finish(); // Ferme l'activité si l'utilisateur n'est pas connecté
            return;
        }

        setContentView(R.layout.new_claim);

        // Gestion du bouton retour
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // Configuration de la barre d'outils
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nouvelle Réclamation");
        }

        // Initialisation de Firebase
        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        submitButton = findViewById(R.id.submitButton);

        // Configuration du bouton de soumission
        submitButton.setOnClickListener(v -> submitClaim());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void submitClaim() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        // Validation des champs
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Désactiver le bouton pendant la soumission
        submitButton.setEnabled(false);

        // Création de l'objet réclamation
        Map<String, Object> claim = new HashMap<>();
        claim.put("title", title);
        claim.put("description", description);
        claim.put("userId", mAuth.getCurrentUser().getUid());
        claim.put("timestamp", System.currentTimeMillis());
        claim.put("status", "pending");

        // Sauvegarde dans Firestore
        db.collection("claims")
            .add(claim)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(ReclamationsActivity.this, 
                    "Réclamation soumise avec succès", Toast.LENGTH_SHORT).show();
                finish(); // Retour à l'écran précédent
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ReclamationsActivity.this,
                    "Erreur lors de la soumission de la réclamation", Toast.LENGTH_SHORT).show();
                submitButton.setEnabled(true); // Réactiver le bouton en cas d'erreur
            });
    }
} 
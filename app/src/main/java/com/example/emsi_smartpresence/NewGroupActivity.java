package com.example.emsi_smartpresence;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NewGroupActivity extends AppCompatActivity {
    private static final String TAG = "NewGroupActivity";
    private TextInputEditText groupNameInput;
    private TextInputEditText groupDescriptionInput;
    private Button createGroupButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        // Configuration de la barre d'outils
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nouveau groupe");
        }

        // Initialisation de Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialisation des vues
        groupNameInput = findViewById(R.id.groupNameInput);
        groupDescriptionInput = findViewById(R.id.groupDescriptionInput);
        createGroupButton = findViewById(R.id.createGroupButton);

        // Configuration du bouton de création
        createGroupButton.setOnClickListener(v -> createGroup());
    }

    private void createGroup() {
        String groupName = groupNameInput.getText().toString().trim();
        String groupDescription = groupDescriptionInput.getText().toString().trim();

        if (TextUtils.isEmpty(groupName)) {
            groupNameInput.setError("Le nom du groupe est requis.");
            groupNameInput.requestFocus();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vous devez être connecté pour créer un groupe.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> group = new HashMap<>();
        group.put("name", groupName);
        group.put("description", groupDescription);
        group.put("createdBy", currentUser.getUid());
        group.put("createdAt", FieldValue.serverTimestamp());
        group.put("lastMessageTime", FieldValue.serverTimestamp()); // Initialiser avec le timestamp de création
        group.put("members", Arrays.asList(currentUser.getUid())); // Ajouter le créateur comme premier membre

        db.collection("groups")
                .add(group)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Group created with ID: " + documentReference.getId());
                    Toast.makeText(this, "Groupe créé avec succès!", Toast.LENGTH_SHORT).show();
                    finish(); // Fermer l'activité après la création
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error creating group", e);
                    Toast.makeText(this, "Erreur lors de la création du groupe.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 
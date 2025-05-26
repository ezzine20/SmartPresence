package com.example.emsi_smartpresence;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.emsi_smartpresence.adapters.DocumentAdapter;
import com.example.emsi_smartpresence.utils.FirestoreManager;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

public class DocumentsActivity extends AppCompatActivity {

    private static final String TAG = "DocumentsActivity";

    private RecyclerView documentsRecyclerView;
    private DocumentAdapter documentAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: DocumentsActivity created");
        setContentView(R.layout.activity_documents);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Documents");
        }

        documentsRecyclerView = findViewById(R.id.documentsRecyclerView);
        documentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        documentAdapter = new DocumentAdapter(new ArrayList<>());
        documentsRecyclerView.setAdapter(documentAdapter);

        fetchDocuments();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchDocuments() {
        // Vérifier si l'utilisateur est connecté avant de charger les documents
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "User not logged in, cannot fetch documents.");
            Toast.makeText(this, "Veuillez vous connecter pour voir les documents.", Toast.LENGTH_LONG).show();
            documentAdapter.setDocuments(new ArrayList<>()); // Vider la liste
            return;
        }
        
        String userId = currentUser.getUid();
        Log.d(TAG, "Fetching documents for user: " + userId);

        // Utilisez getDocumentsForUser si les documents sont spécifiques à l'utilisateur
        // Si tous les documents sont accessibles à tous les utilisateurs connectés, utilisez getAllDocuments().
        // Assurez-vous que vos règles de sécurité correspondent à la méthode utilisée ici.
        
        // Exemple avec getDocumentsForUser:
         FirestoreManager.getDocumentsForUser(userId)

        // Exemple avec getAllDocuments (si règles permettent la lecture à tout connecté):
        // FirestoreManager.getAllDocuments()

                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documentsList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            documentsList.add(document);
                        }
                        Log.d(TAG, "Documents fetched: " + documentsList.size());
                        documentAdapter.setDocuments(documentsList);
                    } else {
                        Log.e(TAG, "Error fetching documents: ", task.getException());
                        runOnUiThread(() -> {
                            Toast.makeText(DocumentsActivity.this, "Erreur lors du chargement des documents.", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }
} 
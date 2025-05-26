package com.example.emsi_smartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.example.emsi_smartpresence.models.User;
import com.example.emsi_smartpresence.utils.FirestoreManager;

public class RegisterActivity extends AppCompatActivity {

    EditText registerEmail, registerPassword, registerFullName;
    Button registerButton, goBackButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialisation Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        registerEmail = findViewById(R.id.register_email);
        registerPassword = findViewById(R.id.register_password);
        registerFullName = findViewById(R.id.register_fullname);
        registerButton = findViewById(R.id.register_button);
        goBackButton = findViewById(R.id.go_back_button);

        registerButton.setOnClickListener(v -> {
            String email = registerEmail.getText().toString().trim();
            String password = registerPassword.getText().toString().trim();
            String fullName = registerFullName.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validation de l'adresse email
            if (!email.endsWith("@emsi-edu.ma")) {
                Toast.makeText(RegisterActivity.this, "Veuillez utiliser une adresse email EMSI valide.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validation de la longueur du mot de passe
            if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Le mot de passe doit contenir au moins 6 caractères.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Création de l'utilisateur Firebase
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Récupérer l'utilisateur créé
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    // Créer un document utilisateur dans Firestore
                                    User user = new User(
                                        firebaseUser.getUid(),
                                        email,
                                        fullName,
                                        "student" // Rôle par défaut
                                    );
                                    FirestoreManager.createUser(user);
                                    
                                    Toast.makeText(RegisterActivity.this, "Inscription réussie.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                }
                            } else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(RegisterActivity.this, "Cette adresse email est déjà utilisée.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Échec de l'inscription: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
        });

        goBackButton.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}



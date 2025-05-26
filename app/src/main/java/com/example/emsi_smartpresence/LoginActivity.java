package com.example.emsi_smartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.ActionCodeResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AlertDialog progressDialog;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialisation Firebase avec la nouvelle configuration
        initializeFirebase();

        // Initialisation du ProgressDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        progressDialog = builder.create();

        // Liaison avec les champs XML
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.btn_login);
        Button registerRedirectButton = findViewById(R.id.btn_register_redirect);

        // Gérer l'intent de vérification d'email (lien profond)
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            String actionLink = intent.getData().toString();
            Log.d(TAG, "Received action link: " + actionLink);
            handleEmailVerificationLink(actionLink);
        } else {
            // Vérifier si l'utilisateur est déjà connecté au démarrage de l'activité (si pas de lien profond)
            checkExistingUserSession();
        }

        // Action bouton "Se connecter"
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (validateInput(email, password)) {
                    performLogin(email, password);
                }
            }
        });

        // Action bouton "S'inscrire"
        registerRedirectButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!email.endsWith("@emsi-edu.ma")) {
            Toast.makeText(this, "Veuillez utiliser une adresse email EMSI valide.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void performLogin(String email, String password) {
        loginButton.setEnabled(false);
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        loginButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "Login successful for user: " + user.getUid());
                                // L'utilisateur est connecté, naviguer vers l'écran d'accueil sans vérifier l'email
                                updateUserStatus(user.getUid());
                                navigateToHome();
                            } else {
                                Log.w(TAG, "signInWithEmailAndPassword successful but currentUser is null.");
                                Toast.makeText(LoginActivity.this, "Une erreur inattendue s'est produite.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String errorMessage = "Échec de la connexion";
                            if (task.getException() != null) {
                                String exceptionMessage = task.getException().getMessage();
                                if (exceptionMessage.contains("password is invalid")) {
                                    errorMessage = "Mot de passe incorrect";
                                } else if (exceptionMessage.contains("no user record")) {
                                    errorMessage = "Aucun compte trouvé avec cet email";
                                } else if (exceptionMessage.contains("badly formatted")) {
                                    errorMessage = "Format d'email invalide";
                                } else if (exceptionMessage.contains("expired")) {
                                    errorMessage = "Session expirée, veuillez réessayer";
                                } else {
                                    errorMessage += ": " + exceptionMessage;
                                }
                                Log.e(TAG, "Login failed: " + exceptionMessage);
                            } else {
                                Log.e(TAG, "Login failed with unknown error.");
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void updateUserStatus(String userId) {
        db.collection("users").document(userId)
                .update("isOnline", true, "lastSeen", System.currentTimeMillis())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user status in Firestore", e);
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeFirebase() {
        // Initialisation de Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Initialisation de Firestore
        db = FirebaseFirestore.getInstance();
    }

    private void showEmailVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email non vérifié")
                .setMessage("Veuillez vérifier votre email pour accéder à l'application. Voulez-vous que nous renvoyions l'email de vérification ?")
                .setPositiveButton("Renvoyer", (dialog, which) -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        sendVerificationEmail(user);
                    }
                })
                .setNegativeButton("Annuler", (dialog, which) -> {
                    mAuth.signOut();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void sendVerificationEmail(FirebaseUser user) {
        progressDialog.show();
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Email de vérification envoyé à " + user.getEmail(),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Échec de l'envoi de l'email de vérification. Veuillez réessayer.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkExistingUserSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Vérifier si l'email est vérifié pour les sessions existantes
            if (currentUser.isEmailVerified()) {
                // L'utilisateur est connecté et vérifié, naviguer vers l'écran d'accueil
                navigateToHome();
            } else {
                // L'utilisateur est connecté mais pas vérifié, déconnecter la session existante
                mAuth.signOut();
                Toast.makeText(this, "Votre email n'est pas vérifié. Veuillez vous reconnecter et vérifier votre email.", Toast.LENGTH_LONG).show();
                // Rester sur l'écran de connexion pour permettre à l'utilisateur de se reconnecter
            }
        }
    }

    private void handleEmailVerificationLink(String link) {
        progressDialog.show();
        mAuth.checkActionCode(link)
            .addOnSuccessListener(result -> {
                @SuppressWarnings("deprecation")
                String email = result.getData(0);
                if (email != null) {
                    mAuth.applyActionCode(link)
                        .addOnSuccessListener(aVoid -> {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Email vérifié avec succès pour " + email, Toast.LENGTH_LONG).show();
                            // After verification, log in the user if necessary and navigate
                            if (mAuth.getCurrentUser() != null) {
                                navigateToHome();
                            } else {
                                // Redirect to the login screen for the user to log in
                                // Email/password fields could be pre-filled if you store the email from the link
                                Toast.makeText(LoginActivity.this, "Votre email est vérifié. Veuillez vous connecter.", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Log.e(TAG, "Error applying action code: ", e);
                            Toast.makeText(LoginActivity.this, "Échec de la vérification de l'email. Le lien est peut-être invalide ou a expiré.", Toast.LENGTH_LONG).show();
                        });
                } else {
                    // Handle the case where email is null
                    progressDialog.dismiss();
                    Log.e(TAG, "Email is null in ActionCodeResult");
                    Toast.makeText(LoginActivity.this, "Une erreur s'est produite lors de la récupération des informations du lien.", Toast.LENGTH_LONG).show();
                }
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Log.e(TAG, "Error checking action code: ", e);
                Toast.makeText(LoginActivity.this, "Échec de la vérification de l'email. Le lien est peut-être invalide ou a expiré.", Toast.LENGTH_LONG).show();
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}

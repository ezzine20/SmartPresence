package com.example.emsi_smartpresence;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialiser Firebase
        FirebaseApp.initializeApp(this);
        
        // Activer la persistance pour Realtime Database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        
        // Initialiser Firestore
        FirebaseFirestore.getInstance();
        
        // Vérification de connexion temporairement désactivée
        /*
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // Rediriger vers LoginActivity
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        */
    }
}

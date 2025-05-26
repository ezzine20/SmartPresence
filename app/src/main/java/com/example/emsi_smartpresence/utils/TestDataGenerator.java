package com.example.emsi_smartpresence.utils;

import android.util.Log;
import com.example.emsi_smartpresence.models.Task;
import java.util.Date;
import java.util.Calendar;

public class TestDataGenerator {
    private static final String TAG = "TestDataGenerator";
    
    public static void generateTestData(String userId) {
        Log.d(TAG, "generateTestData: Starting to generate test data for user: " + userId);
        
        // Générer des tâches de test
        generateTestTasks(userId);
        
        // Générer des annonces de test
        generateTestAnnouncements();
        
        // Générer des messages de test
        generateTestMessages(userId);
        
        // Générer des documents de test
        generateTestDocuments(userId);
    }

    private static void generateTestTasks(String userId) {
        Log.d(TAG, "generateTestTasks: Generating test tasks");
        // Tâche 1
        Task task1 = new Task();
        task1.setTitle("Nettoyage des parties communes");
        task1.setDescription("Nettoyer le hall d'entrée et l'ascenseur");
        task1.setAssignedTo(userId);
        task1.setStatus("pending");
        task1.setPriority("high");
        task1.setWeatherCondition("Ensoleillé");
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 2);
        task1.setDueDate(cal.getTime());
        
        FirestoreManager.addTask(task1);

        // Tâche 2
        Task task2 = new Task();
        task2.setTitle("Vérification des extincteurs");
        task2.setDescription("Vérifier la date de péremption des extincteurs");
        task2.setAssignedTo(userId);
        task2.setStatus("in_progress");
        task2.setPriority("medium");
        task2.setWeatherCondition("Nuageux");
        
        cal.add(Calendar.DAY_OF_MONTH, 5);
        task2.setDueDate(cal.getTime());
        
        FirestoreManager.addTask(task2);
    }

    private static void generateTestAnnouncements() {
        Log.d(TAG, "generateTestAnnouncements: Generating test announcements");
        // Annonce 1
        FirestoreManager.addAnnouncement(
            "Réunion du conseil syndical",
            "Une réunion du conseil syndical aura lieu le 15/03/2024 à 18h00",
            "high"
        );
        Log.d(TAG, "generateTestAnnouncements: Added announcement 1");

        // Annonce 2
        FirestoreManager.addAnnouncement(
            "Travaux d'ascenseur",
            "L'ascenseur sera en maintenance le 10/03/2024 de 9h à 12h",
            "medium"
        );
        Log.d(TAG, "generateTestAnnouncements: Added announcement 2");
    }

    private static void generateTestMessages(String userId) {
        Log.d(TAG, "generateTestMessages: Generating test messages");
        // Message 1
        FirestoreManager.addMessage(
            "admin",
            userId,
            "Bonjour, n'oubliez pas la réunion de demain"
        );

        // Message 2
        FirestoreManager.addMessage(
            "concierge",
            userId,
            "Votre colis est disponible à la réception"
        );
    }

    private static void generateTestDocuments(String userId) {
        Log.d(TAG, "generateTestDocuments: Generating test documents");
        // Document 1
        FirestoreManager.addDocument(
            userId,
            "Règlement intérieur",
            "Règlement intérieur de la copropriété",
            "https://example.com/reglement.pdf"
        );

        // Document 2
        FirestoreManager.addDocument(
            userId,
            "Procès-verbal AG",
            "Procès-verbal de la dernière assemblée générale",
            "https://example.com/pv.pdf"
        );
    }
} 
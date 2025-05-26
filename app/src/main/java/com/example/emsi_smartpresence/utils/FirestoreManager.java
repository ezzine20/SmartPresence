package com.example.emsi_smartpresence.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.CollectionReference;
import com.example.emsi_smartpresence.models.Task;
import com.example.emsi_smartpresence.models.User;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirestoreManager {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String USERS_COLLECTION = "users";

    // Méthodes pour les utilisateurs
    public static void createUser(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", user.getUid());
        userMap.put("email", user.getEmail());
        userMap.put("fullName", user.getFullName());
        userMap.put("role", user.getRole());
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("lastLogin", user.getLastLogin());
        userMap.put("isActive", user.isActive());

        db.collection(USERS_COLLECTION).document(user.getUid()).set(userMap);
    }

    public static void updateUserLastLogin(String userId) {
        db.collection(USERS_COLLECTION).document(userId)
            .update("lastLogin", new Date());
    }

    public static DocumentReference getUserReference(String userId) {
        return db.collection(USERS_COLLECTION).document(userId);
    }

    public static Query getAllUsers() {
        return db.collection(USERS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    public static Query getUsersByRole(String role) {
        return db.collection(USERS_COLLECTION)
                .whereEqualTo("role", role)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    // Méthodes pour les tâches
    public static Query getTasksForUser(String userId) {
        return db.collection("tasks")
                .whereEqualTo("assignedTo", userId)
                .orderBy("dueDate", Query.Direction.ASCENDING);
    }

    public static void addTask(Task task) {
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("title", task.getTitle());
        taskMap.put("description", task.getDescription());
        taskMap.put("assignedTo", task.getAssignedTo());
        taskMap.put("status", task.getStatus());
        taskMap.put("dueDate", task.getDueDate());
        taskMap.put("priority", task.getPriority());
        taskMap.put("weatherCondition", task.getWeatherCondition());

        db.collection("tasks").add(taskMap);
    }

    // Méthodes pour les annonces
    public static Query getAnnouncements(String type) {
        return db.collection("announcements")
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public static void addAnnouncement(String title, String content, String priority) {
        Map<String, Object> announcement = new HashMap<>();
        announcement.put("title", title);
        announcement.put("content", content);
        announcement.put("priority", priority);
        announcement.put("timestamp", new Date());

        db.collection("announcements").add(announcement);
    }

    // Méthodes pour les messages
    public static Query getMessagesForUser(String userId) {
        return db.collection("messages")
                .whereEqualTo("recipientId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public static void addMessage(String senderId, String recipientId, String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("senderId", senderId);
        message.put("recipientId", recipientId);
        message.put("content", content);
        message.put("timestamp", new Date());
        message.put("read", false);

        db.collection("messages").add(message);
    }

    // Méthodes pour les documents
    public static Query getDocumentsForUser(String userId) {
        return db.collection("documents")
                .whereEqualTo("ownerId", userId)
                .orderBy("uploadDate", Query.Direction.DESCENDING);
    }
    
    public static Query getAllDocuments() { // Nouvelle méthode pour les tests
        return db.collection("documents")
                .orderBy("uploadDate", Query.Direction.DESCENDING);
    }

    public static void addDocument(String ownerId, String title, String description, String url) {
        Map<String, Object> document = new HashMap<>();
        document.put("ownerId", ownerId);
        document.put("title", title);
        document.put("description", description);
        document.put("url", url);
        document.put("uploadDate", new Date());

        db.collection("documents").add(document);
    }
} 
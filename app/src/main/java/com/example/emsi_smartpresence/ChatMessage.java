package com.example.emsi_smartpresence;

import java.util.HashMap;
import java.util.Map;

public class ChatMessage {
    private String message;
    private boolean isUser;
    private String senderName;

    // Firestore requires a public no-argument constructor
    public ChatMessage() {}

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public ChatMessage(String message, boolean isUser, String senderName) {
        this.message = message;
        this.isUser = isUser;
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public String getSenderName() {
        return senderName;
    }

    // Méthode pour convertir l'objet ChatMessage en Map pour Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        // Note: isUser n'est pas stocké dans Firestore car il dépend de l'utilisateur actuel
        // Firestore stockera senderId, et l'application déterminera isUser
        // based on the current user's ID
        return map;
    }
} 
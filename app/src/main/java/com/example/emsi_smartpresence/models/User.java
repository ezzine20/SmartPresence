package com.example.emsi_smartpresence.models;

import java.util.Date;

public class User {
    private String uid;
    private String email;
    private String fullName;
    private String role;
    private Date createdAt;
    private Date lastLogin;
    private boolean isActive;

    public User() {
        // Constructeur vide requis pour Firestore
    }

    public User(String uid, String email, String fullName, String role) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = new Date();
        this.lastLogin = new Date();
        this.isActive = true;
    }

    // Getters et Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
} 
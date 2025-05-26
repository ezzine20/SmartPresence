package com.example.emsi_smartpresence.models;

import java.util.Date;

public class Task {
    private String id;
    private String title;
    private String description;
    private String assignedTo;
    private String status;
    private Date dueDate;
    private String priority;
    private String weatherCondition;

    public Task() {
        // Constructeur vide requis pour Firestore
    }

    public Task(String id, String title, String description, String assignedTo, 
                String status, Date dueDate, String priority, String weatherCondition) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.assignedTo = assignedTo;
        this.status = status;
        this.dueDate = dueDate;
        this.priority = priority;
        this.weatherCondition = weatherCondition;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
} 
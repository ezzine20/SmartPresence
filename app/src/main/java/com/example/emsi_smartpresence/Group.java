package com.example.emsi_smartpresence;

import com.google.firebase.Timestamp;

public class Group {
    private String id;
    private String name;
    private String description;
    private Timestamp lastMessageTime;

    public Group(String id, String name, String description, Timestamp lastMessageTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lastMessageTime = lastMessageTime;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getLastMessageTime() {
        return lastMessageTime;
    }
} 
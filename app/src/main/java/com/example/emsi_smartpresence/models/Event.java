package com.example.emsi_smartpresence.models;

public class Event {
    private String id;
    private String title;
    private String location;
    private boolean isAllDay;
    private Long startTime;
    private Long endTime;
    private String userId;

    public Event() {
        // Required empty public constructor for Firestore
    }

    public Event(String id, String title, String location, boolean isAllDay, Long startTime, Long endTime, String userId) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.isAllDay = isAllDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
} 
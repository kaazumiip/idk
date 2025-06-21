package com.example.schedule_application.model;


import com.google.firebase.Timestamp;

import java.util.Date;

public class ActivityLog {
    private String userId;
    private String activity;
    private Date timestamp;
    public ActivityLog() {} // Required for Firestore

    public ActivityLog(String userId, String activity, Date timestamp) {
        this.userId = userId;
        this.activity = activity;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getActivity() {
        return activity;
    }

    public Date getTimestamp() {
        return timestamp;
    }

}


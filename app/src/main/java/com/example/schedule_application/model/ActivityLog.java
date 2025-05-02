package com.example.schedule_application.model;


public class ActivityLog {
    private String activity;
    private String timestamp;

    public ActivityLog(String activity, String timestamp) {
        this.activity = activity;
        this.timestamp = timestamp;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
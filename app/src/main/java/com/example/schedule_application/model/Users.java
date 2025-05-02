package com.example.schedule_application.model;


import java.util.ArrayList;
import java.util.List;

public class Users {
    private String userId;
    private String username;
    private String email;
    private String profileImageUrl;
    private String preferredTheme;
    private boolean notificationsEnabled;

    // Empty constructor for Firebase
    public Users() {
        // Required for Firebase
        notificationsEnabled = true;
        preferredTheme = "light";
    }

    public Users(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.profileImageUrl = "";
        this.preferredTheme = "light";
        this.notificationsEnabled = true;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }



    public String getPreferredTheme() {
        return preferredTheme;
    }

    public void setPreferredTheme(String preferredTheme) {
        this.preferredTheme = preferredTheme;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}
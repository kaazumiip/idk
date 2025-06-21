package com.example.schedule_application.model;

// SuggestedTask.java

import java.io.Serializable;

public class SuggestedTask implements Serializable {
    private String category;
    private String title;
    private String description;

    public SuggestedTask(String category, String title, String description) {
        this.category = category;
        this.title = title;
        this.description = description;
    }

    public SuggestedTask() {}

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

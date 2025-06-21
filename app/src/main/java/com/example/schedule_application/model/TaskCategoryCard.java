package com.example.schedule_application.model;


import java.util.List;

public class TaskCategoryCard {
    public String category;
    public List<SuggestedTask> tasks;

    public TaskCategoryCard(String category, List<SuggestedTask> tasks) {
        this.category = category;
        this.tasks = tasks;
    }
}

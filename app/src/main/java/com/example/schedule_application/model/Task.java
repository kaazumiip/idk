package com.example.schedule_application.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Task implements Serializable {
    private String name;
    private String description;
    private String time;
    private String duration;
    private String category;
    private long timestampMillis;
    private boolean completed;
    private String type;
    private String id;
    private String status;


    public Task() {
    }

    public Task(String name, String description, String time, String duration, String category, Timestamp timestamp, boolean isCompleted) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.time = time;
        this.duration = duration;
        this.category = category;
        this.timestampMillis = timestamp != null ? timestamp.toDate().getTime() : 0;
        this.completed = isCompleted;
        this.status = "Not complete";

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() {
        return time;
    }

    public String getDuration() {
        return duration;
    }

    public String getCategory() {
        return category;
    }

    public Timestamp getTimestamp() {
        return timestampMillis > 0 ? new Timestamp(new Date(timestampMillis)) : null;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getType() {
        return type;
    }



    public String getId() {
        return id;
    }



    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestampMillis = timestamp != null ? timestamp.toDate().getTime() : 0;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setType(String type) {
        this.type = type;
    }



    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(String status){
        this.status = status;
    }
    public String getStatus(){
        return status;
    }




}




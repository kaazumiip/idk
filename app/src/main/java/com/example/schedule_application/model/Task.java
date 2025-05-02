package com.example.schedule_application.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.UUID;

public class Task implements Parcelable {
    private String name;
    private String description;
    private String time;
    private String duration;
    private String category;
    private long timestampMillis;
    private boolean completed;
    private String type;
    private long triggerTime;
    private String id;
    private long reminderTime;
    private long followUpReminder;

    public Task() {}

    public Task(String name, String description, String time, String duration, String category, Timestamp timestamp, boolean isCompleted) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.time = time;
        this.duration = duration;
        this.category = category;
        this.timestampMillis = timestamp != null ? timestamp.toDate().getTime() : 0;
        this.completed = isCompleted;

        this.reminderTime = calculateReminderTime();
        this.followUpReminder = calculateFollowUpReminderTime();
    }

    protected Task(Parcel in) {
        name = in.readString();
        description = in.readString();
        time = in.readString();
        duration = in.readString();
        category = in.readString();
        timestampMillis = in.readLong();
        completed = in.readByte() != 0;
        type = in.readString();
        triggerTime = in.readLong();
        id = in.readString();
        reminderTime = in.readLong();
        followUpReminder = in.readLong();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getTime() { return time; }
    public String getDuration() { return duration; }
    public String getCategory() { return category; }
    public Timestamp getTimestamp() {
        return timestampMillis > 0 ? new Timestamp(new Date(timestampMillis)) : null;
    }
    public boolean isCompleted() { return completed; }
    public String getType() { return type; }
    public long getTriggerTime() { return triggerTime; }
    public String getId() { return id; }
    public long getReminderTime() { return reminderTime; }
    public long getFollowUpReminder() { return followUpReminder; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setTime(String time) { this.time = time; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setCategory(String category) { this.category = category; }
    public void setTimestamp(Timestamp timestamp) {
        this.timestampMillis = timestamp != null ? timestamp.toDate().getTime() : 0;
    }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setType(String type) { this.type = type; }
    public void setTriggerTime(long triggerTime) { this.triggerTime = triggerTime; }
    public void setId(String id) { this.id = id; }

    public String getStatus() {
        return calculateStatus();
    }

    public String calculateStatus() {
        if (timestampMillis == 0 || duration == null) return "unknown";

        long now = System.currentTimeMillis();
        long durationMillis = parseDurationToMillis(duration);
        long endMillis = timestampMillis + durationMillis;

        if (now < timestampMillis) return "upcoming";
        else if (now >= timestampMillis && now <= endMillis) return "ongoing";
        else return "finished";
    }

    private long parseDurationToMillis(String durationStr) {
        try {
            String[] parts = durationStr.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return (hours * 60L + minutes) * 60 * 1000;
        } catch (Exception e) {
            return 0;
        }
    }

    private long calculateReminderTime() {
        return timestampMillis - 15 * 60 * 1000;
    }

    private long calculateFollowUpReminderTime() {
        return timestampMillis + 30 * 60 * 1000;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(time);
        parcel.writeString(duration);
        parcel.writeString(category);
        parcel.writeLong(timestampMillis);
        parcel.writeByte((byte) (completed ? 1 : 0));
        parcel.writeString(type);
        parcel.writeLong(triggerTime);
        parcel.writeString(id);
        parcel.writeLong(reminderTime);
        parcel.writeLong(followUpReminder);
    }
}

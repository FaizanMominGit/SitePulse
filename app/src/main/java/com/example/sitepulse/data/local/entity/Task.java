package com.example.sitepulse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey
    @NonNull
    public String id; // Firebase ID

    public String projectId; // Foreign key logic (manual)
    public String title;
    public String description;
    public boolean isCompleted;
    public long timestamp; // For ordering
    
    // New field for sync status
    public boolean isSynced;

    // No-argument constructor for Firestore
    public Task() {}

    public Task(@NonNull String id, String projectId, String title, String description, boolean isCompleted, long timestamp) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
        this.timestamp = timestamp;
        this.isSynced = false; // Default to false
    }
}
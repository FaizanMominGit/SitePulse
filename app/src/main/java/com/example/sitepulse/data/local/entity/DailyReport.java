package com.example.sitepulse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_reports")
public class DailyReport {
    @PrimaryKey
    @NonNull
    public String id; // UUID or Firebase ID

    public String projectId;
    public String userId;
    public long date; // Timestamp
    public int laborCount;
    public String workDescription;
    public String hindrances;
    public String imagePath; // Local path to the image file
    public String imageUrl; // Cloud URL (after sync)
    
    public boolean isSynced;

    public DailyReport(@NonNull String id, String projectId, String userId, long date, int laborCount, String workDescription, String hindrances, String imagePath) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.date = date;
        this.laborCount = laborCount;
        this.workDescription = workDescription;
        this.hindrances = hindrances;
        this.imagePath = imagePath;
        this.isSynced = false;
    }
}
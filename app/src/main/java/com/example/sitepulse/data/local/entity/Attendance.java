package com.example.sitepulse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "attendance")
public class Attendance {
    @PrimaryKey
    @NonNull
    public String id; // UUID or Firebase ID

    public String userId;
    public String projectId;
    public long clockInTime;
    public long clockOutTime; // 0 if currently working
    public String status; // "PRESENT", "ABSENT", "HALF_DAY"
    public double latitude;
    public double longitude;
    
    // Sync status
    public boolean isSynced;

    public Attendance(@NonNull String id, String userId, String projectId, long clockInTime, long clockOutTime, String status, double latitude, double longitude) {
        this.id = id;
        this.userId = userId;
        this.projectId = projectId;
        this.clockInTime = clockInTime;
        this.clockOutTime = clockOutTime;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isSynced = false;
    }
}
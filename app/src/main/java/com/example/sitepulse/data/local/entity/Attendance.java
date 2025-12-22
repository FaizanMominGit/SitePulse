package com.example.sitepulse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "attendance")
public class Attendance {
    @PrimaryKey
    @NonNull
    public String id;

    public String userId;
    public String projectId;
    public long clockInTime;
    public long clockOutTime;
    public String status;
    public double latitude;
    public double longitude;
    
    public boolean isSynced;

    // No-argument constructor for Firestore
    public Attendance() {}

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
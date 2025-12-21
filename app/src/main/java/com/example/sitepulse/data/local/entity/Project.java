package com.example.sitepulse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects")
public class Project {
    @PrimaryKey
    @NonNull
    public String id; // Firebase Document ID

    public String name;
    public String location;
    public String description;
    
    // Geofencing fields
    public double latitude;
    public double longitude;
    public double radiusMeters; // e.g., 100.0 meters

    public Project(@NonNull String id, String name, String location, String description, double latitude, double longitude, double radiusMeters) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
    }
}
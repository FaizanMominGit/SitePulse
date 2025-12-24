package com.example.sitepulse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects")
public class Project {
    @PrimaryKey
    @NonNull
    public String id = ""; // Initialized to prevent null issues with @NonNull

    public String name;
    public String location;
    public String description;
    
    // Geofencing fields
    public double latitude;
    public double longitude;
    public double radiusMeters; // e.g., 100.0 meters

    // Comma-separated list of User IDs (Engineers)
    public String assignedEngineerIds; 
    
    public boolean isArchived; // New field for archiving

    // No-argument constructor for Firestore
    public Project() {}

    public Project(@NonNull String id, String name, String location, String description, double latitude, double longitude, double radiusMeters, String assignedEngineerIds, boolean isArchived) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
        this.assignedEngineerIds = assignedEngineerIds;
        this.isArchived = isArchived;
    }

    @Ignore
    public Project(@NonNull String id, String name, String location, String description, double latitude, double longitude, double radiusMeters, String assignedEngineerIds) {
        this(id, name, location, description, latitude, longitude, radiusMeters, assignedEngineerIds, false);
    }
    
    @Ignore
    public Project(@NonNull String id, String name, String location, String description, double latitude, double longitude, double radiusMeters) {
        this(id, name, location, description, latitude, longitude, radiusMeters, "", false);
    }
}
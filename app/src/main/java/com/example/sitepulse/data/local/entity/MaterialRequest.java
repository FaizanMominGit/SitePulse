package com.example.sitepulse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "material_requests")
public class MaterialRequest {
    @PrimaryKey
    @NonNull
    public String id;

    public String projectId;
    public String userId;
    public String itemName;
    public double quantity;
    public String unit;
    public String urgency;
    public String status;
    public double estimatedCost; // New field for analytics
    public long date;
    
    public boolean isSynced;

    // No-argument constructor for Firestore
    public MaterialRequest() {}

    public MaterialRequest(@NonNull String id, String projectId, String userId, String itemName, double quantity, String unit, String urgency, String status, double estimatedCost, long date) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.urgency = urgency;
        this.status = status;
        this.estimatedCost = estimatedCost;
        this.date = date;
        this.isSynced = false;
    }
}
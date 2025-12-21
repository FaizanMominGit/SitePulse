package com.example.sitepulse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "material_requests")
public class MaterialRequest {
    @PrimaryKey
    @NonNull
    public String id; // UUID or Firebase ID

    public String projectId;
    public String userId;
    public String itemName; // e.g., Cement
    public double quantity;
    public String unit; // e.g., Bags, Kg
    public String urgency; // Low, Medium, High
    public String status; // PENDING, APPROVED, REJECTED
    public long date; // Timestamp
    
    public boolean isSynced;

    public MaterialRequest(@NonNull String id, String projectId, String userId, String itemName, double quantity, String unit, String urgency, String status, long date) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.urgency = urgency;
        this.status = status;
        this.date = date;
        this.isSynced = false;
    }
}
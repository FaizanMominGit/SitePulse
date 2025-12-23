package com.example.sitepulse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "invoices")
public class Invoice {
    @PrimaryKey
    @NonNull
    public String id; // UUID or Firebase ID

    public String projectId;
    public String invoiceNumber;
    public String clientName;
    public String description;
    public double subtotal;
    public double gstRate; // e.g., 18.0 for 18%
    public double gstAmount;
    public double totalAmount;
    public String status; // DRAFT, SENT, PAID
    public long date;
    
    public boolean isSynced;

    // No-argument constructor for Firestore
    public Invoice() {}

    public Invoice(@NonNull String id, String projectId, String invoiceNumber, String clientName, String description, double subtotal, double gstRate, double gstAmount, double totalAmount, String status, long date) {
        this.id = id;
        this.projectId = projectId;
        this.invoiceNumber = invoiceNumber;
        this.clientName = clientName;
        this.description = description;
        this.subtotal = subtotal;
        this.gstRate = gstRate;
        this.gstAmount = gstAmount;
        this.totalAmount = totalAmount;
        this.status = status;
        this.date = date;
        this.isSynced = false;
    }
}
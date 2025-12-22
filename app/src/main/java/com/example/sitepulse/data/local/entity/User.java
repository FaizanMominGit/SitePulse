package com.example.sitepulse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    public String id = ""; // Initialized to prevent null issues with @NonNull

    public String name;
    public String email;
    public String role; // e.g., "Engineer", "Manager"

    // No-argument constructor required for Firestore deserialization
    public User() {
    }

    public User(@NonNull String id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
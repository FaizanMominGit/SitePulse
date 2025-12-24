package com.example.sitepulse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ManagerDashboardActivity extends AppCompatActivity {

    private Button btnManageProjects, btnViewReports, btnMaterialRequests, btnInvoices;
    private ImageButton btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        btnManageProjects = findViewById(R.id.btnManageProjects);
        btnViewReports = findViewById(R.id.btnViewReports);
        btnMaterialRequests = findViewById(R.id.btnMaterialRequests);
        btnInvoices = findViewById(R.id.btnInvoices);
        btnProfile = findViewById(R.id.btnProfile);

        btnManageProjects.setOnClickListener(v -> {
            startActivity(new Intent(ManagerDashboardActivity.this, ManagerProjectListActivity.class));
        });

        btnViewReports.setOnClickListener(v -> {
            startActivity(new Intent(ManagerDashboardActivity.this, ManagerDprListActivity.class));
        });

        btnMaterialRequests.setOnClickListener(v -> {
            startActivity(new Intent(ManagerDashboardActivity.this, ManagerMaterialListActivity.class));
        });

        btnInvoices.setOnClickListener(v -> {
            startActivity(new Intent(ManagerDashboardActivity.this, InvoiceListActivity.class));
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(ManagerDashboardActivity.this, ProfileActivity.class));
        });
        
        Toast.makeText(this, "Welcome Manager!", Toast.LENGTH_SHORT).show();
    }
}
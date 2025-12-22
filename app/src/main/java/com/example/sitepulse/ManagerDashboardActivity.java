package com.example.sitepulse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ManagerDashboardActivity extends AppCompatActivity {

    private Button btnManageProjects, btnViewReports, btnMaterialRequests, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        btnManageProjects = findViewById(R.id.btnManageProjects);
        btnViewReports = findViewById(R.id.btnViewReports);
        btnMaterialRequests = findViewById(R.id.btnMaterialRequests);
        btnLogout = findViewById(R.id.btnLogout);

        btnManageProjects.setOnClickListener(v -> {
            startActivity(new Intent(ManagerDashboardActivity.this, ManagerProjectListActivity.class));
        });

        btnViewReports.setOnClickListener(v -> {
            startActivity(new Intent(ManagerDashboardActivity.this, ManagerDprListActivity.class));
        });

        btnMaterialRequests.setOnClickListener(v -> {
            startActivity(new Intent(ManagerDashboardActivity.this, ManagerMaterialListActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ManagerDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        
        Toast.makeText(this, "Welcome Manager!", Toast.LENGTH_SHORT).show();
    }
}
package com.example.sitepulse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.DailyReport;
import com.example.sitepulse.data.local.entity.Project;
import com.example.sitepulse.ui.adapter.DprAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class DprListActivity extends AppCompatActivity {

    private RecyclerView rvDprList;
    private FloatingActionButton fabCreateDpr;
    private DprAdapter dprAdapter;
    private AppDatabase db;
    private String currentProjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dpr_list);

        db = AppDatabase.getDatabase(this);
        
        // For now, fetch first project ID. In future, pass via Intent.
        loadCurrentProjectId();

        initViews();
        setupRecyclerView();
    }

    private void loadCurrentProjectId() {
         AppDatabase.databaseWriteExecutor.execute(() -> {
            Project p = db.projectDao().getFirstProject();
            if (p != null) {
                currentProjectId = p.id;
                runOnUiThread(this::loadReports);
            } else {
                runOnUiThread(() -> Toast.makeText(this, "No Project Found", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void initViews() {
        rvDprList = findViewById(R.id.rvDprList);
        fabCreateDpr = findViewById(R.id.fabCreateDpr);

        fabCreateDpr.setOnClickListener(v -> {
            if (currentProjectId != null) {
                Intent intent = new Intent(DprListActivity.this, CreateDprActivity.class);
                intent.putExtra("PROJECT_ID", currentProjectId);
                startActivity(intent);
            } else {
                 Toast.makeText(this, "No Project Selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        dprAdapter = new DprAdapter();
        rvDprList.setLayoutManager(new LinearLayoutManager(this));
        rvDprList.setAdapter(dprAdapter);

        dprAdapter.setOnDprClickListener(report -> {
            // Future: Show details
            Toast.makeText(this, "Clicked Report from: " + report.date, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadReports() {
        if (currentProjectId == null) return;
        
        db.dailyReportDao().getReportsForProject(currentProjectId).observe(this, new Observer<List<DailyReport>>() {
            @Override
            public void onChanged(List<DailyReport> reports) {
                dprAdapter.setReports(reports);
            }
        });
    }
}
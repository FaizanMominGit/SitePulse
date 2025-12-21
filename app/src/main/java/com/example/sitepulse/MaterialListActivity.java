package com.example.sitepulse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.MaterialRequest;
import com.example.sitepulse.data.local.entity.Project;
import com.example.sitepulse.ui.adapter.MaterialAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MaterialListActivity extends AppCompatActivity {

    private RecyclerView rvMaterialList;
    private FloatingActionButton fabCreateRequest;
    private MaterialAdapter materialAdapter;
    private AppDatabase db;
    private String currentProjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_list);

        db = AppDatabase.getDatabase(this);

        loadCurrentProjectId();
        initViews();
        setupRecyclerView();
    }

    private void loadCurrentProjectId() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Project p = db.projectDao().getFirstProject();
            if (p != null) {
                currentProjectId = p.id;
                runOnUiThread(this::loadRequests);
            } else {
                runOnUiThread(() -> Toast.makeText(this, "No Project Found", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void initViews() {
        rvMaterialList = findViewById(R.id.rvMaterialList);
        fabCreateRequest = findViewById(R.id.fabCreateRequest);

        fabCreateRequest.setOnClickListener(v -> {
            if (currentProjectId != null) {
                Intent intent = new Intent(MaterialListActivity.this, CreateMaterialActivity.class);
                intent.putExtra("PROJECT_ID", currentProjectId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No Project Selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        materialAdapter = new MaterialAdapter();
        rvMaterialList.setLayoutManager(new LinearLayoutManager(this));
        rvMaterialList.setAdapter(materialAdapter);

        materialAdapter.setOnItemClickListener(request -> {
            // Future: Show details or approve dialog
            Toast.makeText(this, "Clicked: " + request.itemName, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadRequests() {
        if (currentProjectId == null) return;
        
        db.materialRequestDao().getRequestsForProject(currentProjectId).observe(this, new Observer<List<MaterialRequest>>() {
            @Override
            public void onChanged(List<MaterialRequest> requests) {
                materialAdapter.setRequests(requests);
            }
        });
    }
}
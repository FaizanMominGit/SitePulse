package com.example.sitepulse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Project;
import com.example.sitepulse.data.repository.SyncRepository;
import com.example.sitepulse.ui.adapter.ProjectAdapter;
import com.example.sitepulse.util.NetworkUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class ManagerProjectListActivity extends AppCompatActivity {

    private RecyclerView rvProjects;
    private FloatingActionButton fabAddProject;
    private ProjectAdapter adapter;
    private AppDatabase db;
    private SyncRepository syncRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_project_list);

        db = AppDatabase.getDatabase(this);
        syncRepository = new SyncRepository(db, FirebaseFirestore.getInstance());

        initViews();
        setupRecyclerView();
        
        if (NetworkUtils.isNetworkAvailable(this)) {
            syncRepository.syncProjects(new SyncRepository.SyncCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(ManagerProjectListActivity.this, "Projects synced", Toast.LENGTH_SHORT).show();
                        loadProjects();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(ManagerProjectListActivity.this, "Sync failed", Toast.LENGTH_SHORT).show();
                        loadProjects();
                    });
                }
            });
        } else {
            loadProjects();
        }
    }

    private void initViews() {
        rvProjects = findViewById(R.id.rvProjects);
        fabAddProject = findViewById(R.id.fabAddProject);

        fabAddProject.setOnClickListener(v -> {
            startActivity(new Intent(ManagerProjectListActivity.this, CreateProjectActivity.class));
        });
    }

    private void setupRecyclerView() {
        adapter = new ProjectAdapter(new ProjectAdapter.OnProjectActionListener() {
            @Override
            public void onDeleteClick(Project project) {
                deleteProject(project);
            }

            @Override
            public void onArchiveClick(Project project) {
                archiveProject(project);
            }

            @Override
            public void onItemClick(Project project) {
                Intent intent = new Intent(ManagerProjectListActivity.this, ProjectDetailActivity.class);
                intent.putExtra("PROJECT_ID", project.id);
                startActivity(intent);
            }
        });

        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        rvProjects.setAdapter(adapter);
    }

    private void loadProjects() {
        db.projectDao().getAllProjects().observe(this, projects -> {
            adapter.setProjects(projects);
        });
    }

    private void deleteProject(Project project) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.projectDao().delete(project);
            runOnUiThread(() -> Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show());
        });
    }

    private void archiveProject(Project project) {
        project.isArchived = true;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Update local
            db.projectDao().update(project);
            
            // Sync to Remote
            FirebaseFirestore.getInstance().collection("projects")
                    .document(project.id)
                    .update("isArchived", true)
                    .addOnSuccessListener(aVoid -> runOnUiThread(() -> 
                        Toast.makeText(ManagerProjectListActivity.this, "Project archived", Toast.LENGTH_SHORT).show()))
                    .addOnFailureListener(e -> runOnUiThread(() -> 
                        Toast.makeText(ManagerProjectListActivity.this, "Failed to sync archive status", Toast.LENGTH_SHORT).show()));
        });
    }
}
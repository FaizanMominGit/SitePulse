package com.example.sitepulse;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Project;
import com.example.sitepulse.ui.adapter.EngineerSelectionAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CreateProjectActivity extends AppCompatActivity {

    private EditText etProjectName, etProjectLocation, etProjectDescription, etLatitude, etLongitude, etRadius;
    private SearchView svEngineerSearch;
    private RecyclerView rvEngineers;
    private Button btnSaveProject;
    private TextView tvCreateProjectTitle;

    private AppDatabase localDb;
    private FirebaseFirestore remoteDb;
    private EngineerSelectionAdapter adapter;

    private String currentProjectId;
    private Project currentProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        localDb = AppDatabase.getDatabase(this);
        remoteDb = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        setupSearchView();
        observeLocalEngineers(); // The only data source for the UI

        if (getIntent().hasExtra("PROJECT_ID")) {
            currentProjectId = getIntent().getStringExtra("PROJECT_ID");
            tvCreateProjectTitle.setText("Edit Project");
            loadProjectDetails();
        } else {
            tvCreateProjectTitle.setText("Create Project");
        }

        btnSaveProject.setOnClickListener(v -> saveProject());
    }

    private void initViews() {
        tvCreateProjectTitle = findViewById(R.id.tvCreateProjectTitle);
        etProjectName = findViewById(R.id.etProjectName);
        etProjectLocation = findViewById(R.id.etProjectLocation);
        etProjectDescription = findViewById(R.id.etProjectDescription);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etRadius = findViewById(R.id.etRadius);
        svEngineerSearch = findViewById(R.id.svEngineerSearch);
        rvEngineers = findViewById(R.id.rvEngineers);
        btnSaveProject = findViewById(R.id.btnSaveProject);
    }

    private void setupRecyclerView() {
        adapter = new EngineerSelectionAdapter();
        rvEngineers.setLayoutManager(new LinearLayoutManager(this));
        rvEngineers.setAdapter(adapter);
    }

    private void setupSearchView() {
        svEngineerSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
    }

    private void observeLocalEngineers() {
        localDb.userDao().getAllEngineers().observe(this, engineers -> {
            if (engineers != null) {
                adapter.setEngineers(engineers);
                if (currentProject != null) {
                    setSelectedEngineers();
                }
            }
        });
    }

    private void loadProjectDetails() {
        localDb.projectDao().getProjectById(currentProjectId).observe(this, project -> {
            if (project != null) {
                currentProject = project;
                populateUI();
                setSelectedEngineers();
            }
        });
    }

    private void populateUI() {
        etProjectName.setText(currentProject.name);
        etProjectLocation.setText(currentProject.location);
        etProjectDescription.setText(currentProject.description);
        etLatitude.setText(String.valueOf(currentProject.latitude));
        etLongitude.setText(String.valueOf(currentProject.longitude));
        etRadius.setText(String.valueOf(currentProject.radiusMeters));
    }

    private void setSelectedEngineers() {
        if (currentProject.assignedEngineerIds != null && !currentProject.assignedEngineerIds.isEmpty()) {
            String[] engineerIds = currentProject.assignedEngineerIds.split(",");
            Set<String> selectedIds = adapter.getSelectedEngineerIds();
            selectedIds.clear();
            selectedIds.addAll(Arrays.asList(engineerIds));
            adapter.notifyDataSetChanged();
        }
    }

    private void saveProject() {
        String name = etProjectName.getText().toString().trim();
        String location = etProjectLocation.getText().toString().trim();
        String description = etProjectDescription.getText().toString().trim();
        String latStr = etLatitude.getText().toString().trim();
        String lonStr = etLongitude.getText().toString().trim();
        String radiusStr = etRadius.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(location) || TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lonStr) || TextUtils.isEmpty(radiusStr)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude, longitude, radius;
        try {
            latitude = Double.parseDouble(latStr);
            longitude = Double.parseDouble(lonStr);
            radius = Double.parseDouble(radiusStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for coordinates and radius", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<String> selectedIds = adapter.getSelectedEngineerIds();
        String assignedEngineerIds = TextUtils.join(",", selectedIds);

        String projectId = (currentProjectId != null) ? currentProjectId : UUID.randomUUID().toString();

        Project project = new Project(projectId, name, location, description, latitude, longitude, radius, assignedEngineerIds);

        Map<String, Object> projectMap = new HashMap<>();
        projectMap.put("id", project.id);
        projectMap.put("name", project.name);
        projectMap.put("location", project.location);
        projectMap.put("description", project.description);
        projectMap.put("latitude", project.latitude);
        projectMap.put("longitude", project.longitude);
        projectMap.put("radiusMeters", project.radiusMeters);
        projectMap.put("assignedEngineerIds", project.assignedEngineerIds);

        remoteDb.collection("projects").document(projectId).set(projectMap)
                .addOnSuccessListener(aVoid -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        localDb.projectDao().insertAll(java.util.Collections.singletonList(project));
                        runOnUiThread(() -> {
                            Toast.makeText(CreateProjectActivity.this, "Project saved successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateProjectActivity.this, "Failed to save project: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
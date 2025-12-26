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
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Project;
import com.example.sitepulse.ui.adapter.EngineerSelectionAdapter;
import com.example.sitepulse.util.NotificationTrigger;
import com.example.sitepulse.worker.SyncWorker;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CreateProjectActivity extends AppCompatActivity {

    private EditText etProjectName, etProjectLocation, etProjectDescription, etLatitude, etLongitude, etRadius;
    private SearchView svEngineerSearch;
    private RecyclerView rvEngineers;
    private Button btnSaveProject;
    private TextView tvCreateProjectTitle;

    private AppDatabase localDb;
    private EngineerSelectionAdapter adapter;

    private String currentProjectId;
    private Project currentProject;
    private Set<String> initialEngineerIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        localDb = AppDatabase.getDatabase(this);

        initViews();
        setupRecyclerView();
        setupSearchView();
        observeLocalEngineers();

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
            initialEngineerIds.clear();
            initialEngineerIds.addAll(Arrays.asList(engineerIds));
            adapter.setSelectedEngineerIds(initialEngineerIds);
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

        Set<String> finalEngineerIds = adapter.getSelectedEngineerIds();
        String assignedEngineerIdsStr = TextUtils.join(",", finalEngineerIds);

        String projectId = (currentProjectId != null) ? currentProjectId : UUID.randomUUID().toString();

        Project project = new Project(projectId, name, location, description, latitude, longitude, radius, assignedEngineerIdsStr, false, false);

        // Save to local DB first
        AppDatabase.databaseWriteExecutor.execute(() -> {
            localDb.projectDao().insertAll(Collections.singletonList(project));
            triggerSync();

            // Trigger notifications for newly assigned engineers
            for (String engineerId : finalEngineerIds) {
                if (!initialEngineerIds.contains(engineerId)) {
                    String title = "New Project Assignment";
                    String body = "You have been assigned to a new project: " + name;
                    NotificationTrigger.sendNotification(engineerId, title, body);
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(CreateProjectActivity.this, "Project saved!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void triggerSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(this).enqueue(syncRequest);
    }
}
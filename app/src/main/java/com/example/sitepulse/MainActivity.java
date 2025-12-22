package com.example.sitepulse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Project;
import com.example.sitepulse.data.local.entity.Task;
import com.example.sitepulse.data.local.entity.User;
import com.example.sitepulse.data.repository.SyncRepository;
import com.example.sitepulse.ui.adapter.TaskAdapter;
import com.example.sitepulse.util.NetworkUtils;
import com.example.sitepulse.worker.SyncWorker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvDate, tvLocation;
    private Spinner spinnerProjects;
    private RecyclerView rvTasks;
    private Button btnLogout, btnOpenAttendance, btnOpenDpr, btnOpenMaterials;
    private FloatingActionButton fabAddTask;
    private ProgressBar pbProjectSync;

    private TaskAdapter taskAdapter;
    private AppDatabase db;
    private FirebaseAuth mAuth;
    private SyncRepository syncRepository;
    private Project currentProject;
    private List<Project> assignedProjects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = AppDatabase.getDatabase(this);
        syncRepository = new SyncRepository(db, FirebaseFirestore.getInstance());

        initViews();
        setupRecyclerView();
        loadUserData();

        if (NetworkUtils.isNetworkAvailable(this)) {
            pbProjectSync.setVisibility(View.VISIBLE);
            syncRepository.syncProjects(new SyncRepository.SyncCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        pbProjectSync.setVisibility(View.GONE);
                        loadAssignedProjects();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        pbProjectSync.setVisibility(View.GONE);
                        loadAssignedProjects(); // Fallback to local data
                    });
                }
            });
        } else {
            loadAssignedProjects();
        }

        schedulePeriodicSync();

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        fabAddTask.setOnClickListener(v -> {
            addDummyTask();
        });

        btnOpenAttendance.setOnClickListener(v -> {
            if (currentProject != null) {
                Intent intent = new Intent(MainActivity.this, AttendanceActivity.class);
                intent.putExtra("PROJECT_ID", currentProject.id);
                startActivity(intent);
            }
        });

        btnOpenDpr.setOnClickListener(v -> {
            if (currentProject != null) {
                Intent intent = new Intent(MainActivity.this, DprListActivity.class);
                intent.putExtra("PROJECT_ID", currentProject.id);
                startActivity(intent);
            }
        });

        btnOpenMaterials.setOnClickListener(v -> {
            if (currentProject != null) {
                Intent intent = new Intent(MainActivity.this, MaterialListActivity.class);
                intent.putExtra("PROJECT_ID", currentProject.id);
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvDate = findViewById(R.id.tvDate);
        spinnerProjects = findViewById(R.id.spinnerProjects);
        tvLocation = findViewById(R.id.tvLocation);
        rvTasks = findViewById(R.id.rvTasks);
        btnLogout = findViewById(R.id.btnLogout);
        fabAddTask = findViewById(R.id.fabAddTask);
        btnOpenAttendance = findViewById(R.id.btnOpenAttendance);
        btnOpenDpr = findViewById(R.id.btnOpenDpr);
        btnOpenMaterials = findViewById(R.id.btnOpenMaterials);
        pbProjectSync = findViewById(R.id.pbProjectSync);

        String currentDate = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(new Date());
        tvDate.setText(currentDate);
    }
    
    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter();
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(taskAdapter);
        
        taskAdapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                Toast.makeText(MainActivity.this, "Clicked: " + task.title, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTaskChecked(Task task, boolean isChecked) {
                task.isCompleted = isChecked;
                task.isSynced = false;
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    db.taskDao().update(task);
                    triggerImmediateSync();
                });
            }
        });
    }

    private void schedulePeriodicSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest =
                new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueue(syncRequest);
    }

    private void triggerImmediateSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest =
                new OneTimeWorkRequest.Builder(SyncWorker.class)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueue(syncRequest);
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.userDao().getUser(currentUser.getUid()).observe(this, user -> {
                if (user != null) {
                    tvWelcome.setText("Hello, " + user.name);
                } else {
                    tvWelcome.setText("Hello, User");
                }
            });
        }
    }
    
    private void loadAssignedProjects() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.projectDao().getProjectsForEngineer(user.getUid()).observe(this, projects -> {
                if (projects != null && !projects.isEmpty()) {
                    assignedProjects = projects;
                    setupProjectSpinner();
                } else {
                    tvLocation.setText("No projects assigned");
                }
            });
        }
    }

    private void setupProjectSpinner() {
        List<String> projectNames = new ArrayList<>();
        for (Project project : assignedProjects) {
            projectNames.add(project.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, projectNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProjects.setAdapter(adapter);

        spinnerProjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentProject = assignedProjects.get(position);
                updateProjectDetails();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentProject = null;
                updateProjectDetails();
            }
        });
        
        // Set initial selection if any
        if (!assignedProjects.isEmpty()) {
            currentProject = assignedProjects.get(0);
            updateProjectDetails();
        }
    }

    private void updateProjectDetails() {
        if (currentProject != null) {
            tvLocation.setText(currentProject.location);
            loadTasksForProject(currentProject.id);
        } else {
            tvLocation.setText("Select a project");
            taskAdapter.setTasks(new ArrayList<>()); // Clear tasks
        }
    }

    private void loadTasksForProject(String projectId) {
        db.taskDao().getTasksForProject(projectId).observe(this, tasks -> {
            taskAdapter.setTasks(tasks);
        });
    }

    private void addDummyTask() {
        if (currentProject != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                Task task = new Task(
                        UUID.randomUUID().toString(),
                        currentProject.id,
                        "New Task " + System.currentTimeMillis(),
                        "This is a generated task description.",
                        false,
                        System.currentTimeMillis()
                );
                db.taskDao().insertAll(java.util.Collections.singletonList(task));
                triggerImmediateSync();
            });
        } else {
            Toast.makeText(this, "No project selected to add task", Toast.LENGTH_SHORT).show();
        }
    }
}
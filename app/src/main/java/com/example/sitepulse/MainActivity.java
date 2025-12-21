package com.example.sitepulse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
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
import com.example.sitepulse.ui.adapter.TaskAdapter;
import com.example.sitepulse.worker.SyncWorker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvDate, tvProjectName, tvLocation;
    private RecyclerView rvTasks;
    private Button btnLogout, btnOpenAttendance, btnOpenDpr, btnOpenMaterials;
    private FloatingActionButton fabAddTask;
    
    private TaskAdapter taskAdapter;
    private AppDatabase db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = AppDatabase.getDatabase(this);

        initViews();
        setupRecyclerView();
        loadUserData();
        loadTasks();
        schedulePeriodicSync(); // Start background sync
        
        // Ensure we have a dummy project for testing
        addDummyProject();

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        fabAddTask.setOnClickListener(v -> {
            addDummyTask();
        });
        
        btnOpenAttendance.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AttendanceActivity.class));
        });

        btnOpenDpr.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DprListActivity.class));
        });
        
        btnOpenMaterials.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MaterialListActivity.class));
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

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvDate = findViewById(R.id.tvDate);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvLocation = findViewById(R.id.tvLocation);
        rvTasks = findViewById(R.id.rvTasks);
        btnLogout = findViewById(R.id.btnLogout);
        fabAddTask = findViewById(R.id.fabAddTask);
        btnOpenAttendance = findViewById(R.id.btnOpenAttendance);
        btnOpenDpr = findViewById(R.id.btnOpenDpr);
        btnOpenMaterials = findViewById(R.id.btnOpenMaterials);

        // Set Date
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
                // Update task status in DB
                task.isCompleted = isChecked;
                task.isSynced = false; // Mark as unsynced so it gets pushed
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    db.taskDao().update(task);
                    triggerImmediateSync(); // Sync update immediately if possible
                });
            }
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Try to get user from local DB first for offline support
            db.userDao().getUser(currentUser.getUid()).observe(this, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        tvWelcome.setText("Hello, " + user.name);
                    } else {
                        tvWelcome.setText("Hello, User");
                    }
                }
            });
        }
    }

    private void loadTasks() {
        // Observe tasks from Room DB
        db.taskDao().getAllTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                taskAdapter.setTasks(tasks);
            }
        });
    }

    private void addDummyTask() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Task task = new Task(
                    UUID.randomUUID().toString(),
                    "project_1",
                    "New Task " + System.currentTimeMillis(),
                    "This is a generated task description.",
                    false,
                    System.currentTimeMillis()
            );
            // isSynced is false by default in constructor now
            db.taskDao().insertAll(java.util.Collections.singletonList(task));
            triggerImmediateSync(); // Try to sync immediately
        });
    }
    
    private void addDummyProject() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Check if projects exist, if not add one with your coordinates
            if (db.projectDao().getFirstProject() == null) {
                Project project = new Project(
                        "project_1",
                        "City Center Mall",
                        "Downtown Sector 4",
                        "Main construction site",
                        19.152366, // Your supplied Lat
                        72.995016, // Your supplied Lng
                        200.0 // Radius in meters
                );
                db.projectDao().insertAll(java.util.Collections.singletonList(project));
            }
        });
    }
}
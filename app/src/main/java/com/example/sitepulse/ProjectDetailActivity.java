package com.example.sitepulse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Task;
import com.example.sitepulse.ui.adapter.TaskAdapter;
import com.example.sitepulse.ui.viewmodel.TaskViewModel;
import com.example.sitepulse.worker.SyncWorker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProjectDetailActivity extends AppCompatActivity {

    private TextView tvProjectNameDetail;
    private RecyclerView rvProjectTasks;
    private FloatingActionButton fabAddTaskDetail;
    private TaskAdapter taskAdapter;
    private TaskViewModel taskViewModel;
    private AppDatabase db;
    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        db = AppDatabase.getDatabase(this);

        if (getIntent().hasExtra("PROJECT_ID")) {
            projectId = getIntent().getStringExtra("PROJECT_ID");
        } else {
            Toast.makeText(this, "Project ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadProjectDetails();
        setupViewModel();
    }

    private void initViews() {
        tvProjectNameDetail = findViewById(R.id.tvProjectNameDetail);
        rvProjectTasks = findViewById(R.id.rvProjectTasks);
        fabAddTaskDetail = findViewById(R.id.fabAddTaskDetail);

        fabAddTaskDetail.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectDetailActivity.this, CreateTaskActivity.class);
            intent.putExtra("PROJECT_ID", projectId);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter();
        rvProjectTasks.setLayoutManager(new LinearLayoutManager(this));
        rvProjectTasks.setAdapter(taskAdapter);
        
        taskAdapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                 // Optional: Edit task details
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

    private void loadProjectDetails() {
        db.projectDao().getProjectById(projectId).observe(this, project -> {
            if (project != null) {
                tvProjectNameDetail.setText(project.name);
            }
        });
    }

    private void setupViewModel() {
        TaskViewModel.Factory factory = new TaskViewModel.Factory(getApplication(), projectId);
        taskViewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);
        taskViewModel.getTasksForProject().observe(this, tasks -> {
            taskAdapter.setTasks(tasks);
        });
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
}
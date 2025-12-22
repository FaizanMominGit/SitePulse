package com.example.sitepulse;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Task;
import com.example.sitepulse.worker.SyncWorker;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Collections;
import java.util.UUID;

public class CreateTaskActivity extends AppCompatActivity {

    private TextInputEditText etTaskTitle, etTaskDescription;
    private Button btnSaveTask;
    private AppDatabase db;
    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        db = AppDatabase.getDatabase(this);

        if (getIntent().hasExtra("PROJECT_ID")) {
            projectId = getIntent().getStringExtra("PROJECT_ID");
        } else {
            Toast.makeText(this, "Project ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
    }

    private void initViews() {
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();

        if (title.isEmpty()) {
            etTaskTitle.setError("Title cannot be empty");
            etTaskTitle.requestFocus();
            return;
        }

        Task task = new Task(
                UUID.randomUUID().toString(),
                projectId,
                title,
                description,
                false,
                System.currentTimeMillis()
        );

        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.taskDao().insertAll(Collections.singletonList(task));
            runOnUiThread(() -> {
                Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
                triggerImmediateSync();
                finish();
            });
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

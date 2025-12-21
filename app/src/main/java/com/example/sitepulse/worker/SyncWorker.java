package com.example.sitepulse.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Attendance;
import com.example.sitepulse.data.local.entity.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class SyncWorker extends Worker {

    private AppDatabase db;
    private FirebaseFirestore firestore;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = AppDatabase.getDatabase(context);
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        boolean taskSyncSuccess = syncTasks();
        boolean attendanceSyncSuccess = syncAttendance();

        if (taskSyncSuccess && attendanceSyncSuccess) {
            return Result.success();
        } else {
            return Result.retry();
        }
    }

    private boolean syncTasks() {
        List<Task> unsyncedTasks = db.taskDao().getUnsyncedTasks();
        if (unsyncedTasks.isEmpty()) return true;

        CountDownLatch latch = new CountDownLatch(unsyncedTasks.size());
        boolean[] hasErrors = {false};

        for (Task task : unsyncedTasks) {
            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("id", task.id);
            taskMap.put("projectId", task.projectId);
            taskMap.put("title", task.title);
            taskMap.put("description", task.description);
            taskMap.put("isCompleted", task.isCompleted);
            taskMap.put("timestamp", task.timestamp);

            firestore.collection("tasks").document(task.id)
                    .set(taskMap)
                    .addOnSuccessListener(aVoid -> {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            task.isSynced = true;
                            db.taskDao().update(task);
                            latch.countDown();
                        });
                    })
                    .addOnFailureListener(e -> {
                        hasErrors[0] = true;
                        latch.countDown();
                    });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            return false;
        }

        return !hasErrors[0];
    }

    private boolean syncAttendance() {
        List<Attendance> unsyncedAttendance = db.attendanceDao().getUnsyncedAttendance();
        if (unsyncedAttendance.isEmpty()) return true;

        CountDownLatch latch = new CountDownLatch(unsyncedAttendance.size());
        boolean[] hasErrors = {false};

        for (Attendance att : unsyncedAttendance) {
            Map<String, Object> attMap = new HashMap<>();
            attMap.put("id", att.id);
            attMap.put("userId", att.userId);
            attMap.put("projectId", att.projectId);
            attMap.put("clockInTime", att.clockInTime);
            attMap.put("clockOutTime", att.clockOutTime);
            attMap.put("status", att.status);
            attMap.put("latitude", att.latitude);
            attMap.put("longitude", att.longitude);

            firestore.collection("attendance").document(att.id)
                    .set(attMap)
                    .addOnSuccessListener(aVoid -> {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            att.isSynced = true;
                            db.attendanceDao().update(att);
                            latch.countDown();
                        });
                    })
                    .addOnFailureListener(e -> {
                        hasErrors[0] = true;
                        latch.countDown();
                    });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            return false;
        }

        return !hasErrors[0];
    }
}
package com.example.sitepulse.worker;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Attendance;
import com.example.sitepulse.data.local.entity.DailyReport;
import com.example.sitepulse.data.local.entity.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class SyncWorker extends Worker {

    private AppDatabase db;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = AppDatabase.getDatabase(context);
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        boolean taskSyncSuccess = syncTasks();
        boolean attendanceSyncSuccess = syncAttendance();
        boolean reportSyncSuccess = syncDailyReports();

        if (taskSyncSuccess && attendanceSyncSuccess && reportSyncSuccess) {
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

    private boolean syncDailyReports() {
        List<DailyReport> unsyncedReports = db.dailyReportDao().getUnsyncedReports();
        if (unsyncedReports.isEmpty()) return true;

        CountDownLatch latch = new CountDownLatch(unsyncedReports.size());
        boolean[] hasErrors = {false};

        for (DailyReport report : unsyncedReports) {
            uploadImageAndSyncReport(report, latch, hasErrors);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            return false;
        }

        return !hasErrors[0];
    }

    private void uploadImageAndSyncReport(DailyReport report, CountDownLatch latch, boolean[] hasErrors) {
        if (report.imagePath != null && !report.imagePath.isEmpty()) {
            // Upload Image First
            File file = new File(report.imagePath);
            if (!file.exists()) {
                // File lost? Sync without image or fail? Let's sync without image for now.
                syncReportToFirestore(report, null, latch, hasErrors);
                return;
            }

            Uri fileUri = Uri.fromFile(file);
            StorageReference storageRef = storage.getReference().child("reports/" + report.id + ".jpg");

            storageRef.putFile(fileUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return storageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        syncReportToFirestore(report, downloadUrl, latch, hasErrors);
                    })
                    .addOnFailureListener(e -> {
                        hasErrors[0] = true;
                        latch.countDown();
                    });

        } else {
            // No image, just sync data
            syncReportToFirestore(report, null, latch, hasErrors);
        }
    }

    private void syncReportToFirestore(DailyReport report, String imageUrl, CountDownLatch latch, boolean[] hasErrors) {
        Map<String, Object> reportMap = new HashMap<>();
        reportMap.put("id", report.id);
        reportMap.put("projectId", report.projectId);
        reportMap.put("userId", report.userId);
        reportMap.put("date", report.date);
        reportMap.put("laborCount", report.laborCount);
        reportMap.put("workDescription", report.workDescription);
        reportMap.put("hindrances", report.hindrances);
        if (imageUrl != null) {
            reportMap.put("imageUrl", imageUrl);
        }

        firestore.collection("daily_reports").document(report.id)
                .set(reportMap)
                .addOnSuccessListener(aVoid -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        report.isSynced = true;
                        if (imageUrl != null) {
                            report.imageUrl = imageUrl;
                        }
                        db.dailyReportDao().update(report);
                        latch.countDown();
                    });
                })
                .addOnFailureListener(e -> {
                    hasErrors[0] = true;
                    latch.countDown();
                });
    }
}
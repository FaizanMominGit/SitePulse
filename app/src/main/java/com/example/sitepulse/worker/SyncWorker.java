package com.example.sitepulse.worker;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Attendance;
import com.example.sitepulse.data.local.entity.DailyReport;
import com.example.sitepulse.data.local.entity.Invoice;
import com.example.sitepulse.data.local.entity.MaterialRequest;
import com.example.sitepulse.data.local.entity.Project;
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
        boolean projectSyncSuccess = syncProjects();
        boolean taskSyncSuccess = syncTasks();
        boolean attendanceSyncSuccess = syncAttendance();
        boolean reportSyncSuccess = syncDailyReports();
        boolean materialSyncSuccess = syncMaterialRequests();
        boolean invoiceSyncSuccess = syncInvoices(); // Added this

        if (projectSyncSuccess && taskSyncSuccess && attendanceSyncSuccess && reportSyncSuccess && materialSyncSuccess && invoiceSyncSuccess) {
            return Result.success();
        } else {
            return Result.retry();
        }
    }

    private boolean syncInvoices() {
        List<Invoice> unsyncedInvoices = db.invoiceDao().getUnsyncedInvoices();
        if (unsyncedInvoices.isEmpty()) return true;

        CountDownLatch latch = new CountDownLatch(unsyncedInvoices.size());
        boolean[] hasErrors = {false};

        for (Invoice invoice : unsyncedInvoices) {
            Map<String, Object> invoiceMap = new HashMap<>();
            invoiceMap.put("id", invoice.id);
            invoiceMap.put("projectId", invoice.projectId);
            invoiceMap.put("invoiceNumber", invoice.invoiceNumber);
            invoiceMap.put("clientName", invoice.clientName);
            invoiceMap.put("description", invoice.description);
            invoiceMap.put("subtotal", invoice.subtotal);
            invoiceMap.put("gstRate", invoice.gstRate);
            invoiceMap.put("gstAmount", invoice.gstAmount);
            invoiceMap.put("totalAmount", invoice.totalAmount);
            invoiceMap.put("status", invoice.status);
            invoiceMap.put("date", invoice.date);

            firestore.collection("invoices").document(invoice.id)
                    .set(invoiceMap)
                    .addOnSuccessListener(aVoid -> {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            invoice.isSynced = true;
                            db.invoiceDao().update(invoice);
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

    private boolean syncProjects() {
        List<Project> unsyncedProjects = db.projectDao().getUnsyncedProjects();
        if (unsyncedProjects.isEmpty()) return true;

        CountDownLatch latch = new CountDownLatch(unsyncedProjects.size());
        boolean[] hasErrors = {false};

        for (Project project : unsyncedProjects) {
            Map<String, Object> projectMap = new HashMap<>();
            projectMap.put("id", project.id);
            projectMap.put("name", project.name);
            projectMap.put("location", project.location);
            projectMap.put("description", project.description);
            projectMap.put("latitude", project.latitude);
            projectMap.put("longitude", project.longitude);
            projectMap.put("radiusMeters", project.radiusMeters);
            projectMap.put("assignedEngineerIds", project.assignedEngineerIds);
            projectMap.put("isArchived", project.isArchived);

            firestore.collection("projects").document(project.id)
                    .set(projectMap)
                    .addOnSuccessListener(aVoid -> {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            project.isSynced = true;
                            db.projectDao().update(project);
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
            File file = new File(report.imagePath);
            if (!file.exists()) {
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
    
    private boolean syncMaterialRequests() {
        List<MaterialRequest> unsyncedRequests = db.materialRequestDao().getUnsyncedRequests();
        if (unsyncedRequests.isEmpty()) return true;

        CountDownLatch latch = new CountDownLatch(unsyncedRequests.size());
        boolean[] hasErrors = {false};

        for (MaterialRequest request : unsyncedRequests) {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("id", request.id);
            requestMap.put("projectId", request.projectId);
            requestMap.put("userId", request.userId);
            requestMap.put("itemName", request.itemName);
            requestMap.put("quantity", request.quantity);
            requestMap.put("unit", request.unit);
            requestMap.put("urgency", request.urgency);
            requestMap.put("status", request.status);
            requestMap.put("date", request.date);

            firestore.collection("material_requests").document(request.id)
                    .set(requestMap)
                    .addOnSuccessListener(aVoid -> {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            request.isSynced = true;
                            db.materialRequestDao().update(request);
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
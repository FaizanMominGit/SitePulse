package com.example.sitepulse.data.repository;

import android.util.Log;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Attendance;
import com.example.sitepulse.data.local.entity.DailyReport;
import com.example.sitepulse.data.local.entity.Invoice;
import com.example.sitepulse.data.local.entity.MaterialRequest;
import com.example.sitepulse.data.local.entity.Project;
import com.example.sitepulse.data.local.entity.User;
import com.example.sitepulse.util.NotificationTrigger;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SyncRepository {
    private static final String TAG = "SyncRepository";
    private final AppDatabase localDb;
    private final FirebaseFirestore remoteDb;
    private final List<ListenerRegistration> listeners = new ArrayList<>();

    public SyncRepository(AppDatabase localDb, FirebaseFirestore remoteDb) {
        this.localDb = localDb;
        this.remoteDb = remoteDb;
    }

    public interface SyncCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void syncAllData(String userId, SyncCallback callback) {
        new Thread(() -> {
            try {
                syncUsersInternal();
                syncProjectsInternal();
                syncDprsInternal();
                if (userId != null) {
                    syncAttendanceInternal(userId);
                }
                syncMaterialRequestsInternal();
                syncTasksInternal();
                syncInvoicesInternal();
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Sync failed", e);
                callback.onFailure(e);
            }
        }).start();
    }

    // --- Public Granular Sync Methods ---

    public void syncProjects(SyncCallback callback) {
        new Thread(() -> {
            try {
                syncProjectsInternal();
                callback.onSuccess();
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }

    public void syncUsers(SyncCallback callback) {
        new Thread(() -> {
            try {
                syncUsersInternal();
                callback.onSuccess();
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }

    public void syncDprs(SyncCallback callback) {
        new Thread(() -> {
            try {
                syncDprsInternal();
                callback.onSuccess();
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }

    public void syncAttendanceForUser(String userId, SyncCallback callback) {
        new Thread(() -> {
            try {
                syncAttendanceInternal(userId);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }

    public void syncMaterialRequests(SyncCallback callback) {
        new Thread(() -> {
            try {
                syncMaterialRequestsInternal();
                callback.onSuccess();
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }

    public void syncTasks(SyncCallback callback) {
        new Thread(() -> {
            try {
                syncTasksInternal();
                callback.onSuccess();
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }

    public void syncInvoices(SyncCallback callback) {
        new Thread(() -> {
            try {
                syncInvoicesInternal();
                callback.onSuccess();
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }
    
    public void updateRequestStatus(MaterialRequest request, String newStatus, SyncCallback callback) {
        remoteDb.collection("material_requests").document(request.id)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Update local DB
                    request.status = newStatus;
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        localDb.materialRequestDao().update(request);
                        // Trigger Notification
                        String title = "Material Request Updated";
                        String body = "Your request for '" + request.itemName + "' has been " + newStatus + ".";
                        NotificationTrigger.sendNotification(request.userId, title, body);
                        
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    // --- Internal Logic ---

    private void syncUsersInternal() throws ExecutionException, InterruptedException {
        List<User> users = fetchUsers();
        localDb.userDao().insertAll(users);
        Log.d(TAG, "Synced " + users.size() + " users.");
    }

    private void syncProjectsInternal() throws ExecutionException, InterruptedException {
        // Upload local changes first
        List<Project> unsynced = localDb.projectDao().getUnsyncedProjects();
        for (Project p : unsynced) {
            remoteDb.collection("projects").document(p.id).set(p);
            p.isSynced = true;
            localDb.projectDao().update(p);
        }

        // Download remote changes
        List<Project> projects = fetchProjects();
        localDb.projectDao().insertAll(projects);
        Log.d(TAG, "Synced " + projects.size() + " projects.");
    }

    private void syncDprsInternal() throws ExecutionException, InterruptedException {
        List<DailyReport> reports = fetchDailyReports();
        localDb.dailyReportDao().insertAll(reports);
        Log.d(TAG, "Synced " + reports.size() + " daily reports.");
    }

    private void syncAttendanceInternal(String userId) throws ExecutionException, InterruptedException {
        List<Attendance> attendanceList = fetchAttendance(userId);
        for (Attendance a : attendanceList) {
            localDb.attendanceDao().insert(a);
        }
        Log.d(TAG, "Synced " + attendanceList.size() + " attendance records.");
    }

    private void syncMaterialRequestsInternal() throws ExecutionException, InterruptedException {
        List<MaterialRequest> requests = fetchMaterialRequests();
        localDb.materialRequestDao().insertAll(requests);
        Log.d(TAG, "Synced " + requests.size() + " material requests.");
    }

    private void syncTasksInternal() throws ExecutionException, InterruptedException {
        List<com.example.sitepulse.data.local.entity.Task> tasks = fetchTasks();
        localDb.taskDao().insertAll(tasks);
        Log.d(TAG, "Synced " + tasks.size() + " tasks.");
    }

    private void syncInvoicesInternal() throws ExecutionException, InterruptedException {
        List<Invoice> invoices = fetchInvoices();
        localDb.invoiceDao().insertAll(invoices);
        Log.d(TAG, "Synced " + invoices.size() + " invoices.");
    }

    public void startRealtimeSync() {
        ListenerRegistration userListener = remoteDb.collection("users").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "User listen failed.", e);
                return;
            }
            if (snapshots != null) {
                List<User> users = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    users.add(doc.toObject(User.class));
                }
                AppDatabase.databaseWriteExecutor.execute(() -> localDb.userDao().insertAll(users));
                Log.d(TAG, "Real-time: Synced " + users.size() + " users.");
            }
        });
        listeners.add(userListener);

        ListenerRegistration projectListener = remoteDb.collection("projects").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Project listen failed.", e);
                return;
            }
            if (snapshots != null) {
                List<Project> projects = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    Project p = doc.toObject(Project.class);
                    p.isSynced = true; // Mark as synced from server
                    projects.add(p);
                }
                AppDatabase.databaseWriteExecutor.execute(() -> localDb.projectDao().insertAll(projects));
                Log.d(TAG, "Real-time: Synced " + projects.size() + " projects.");
            }
        });
        listeners.add(projectListener);

        ListenerRegistration materialListener = remoteDb.collection("material_requests").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Material Request listen failed.", e);
                return;
            }
            if (snapshots != null) {
                List<MaterialRequest> requests = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    MaterialRequest r = doc.toObject(MaterialRequest.class);
                    r.isSynced = true;
                    requests.add(r);
                }
                AppDatabase.databaseWriteExecutor.execute(() -> localDb.materialRequestDao().insertAll(requests));
                Log.d(TAG, "Real-time: Synced " + requests.size() + " material requests.");
            }
        });
        listeners.add(materialListener);

        ListenerRegistration taskListener = remoteDb.collection("tasks").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Task listen failed.", e);
                return;
            }
            if (snapshots != null) {
                List<com.example.sitepulse.data.local.entity.Task> tasks = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    com.example.sitepulse.data.local.entity.Task t = doc.toObject(com.example.sitepulse.data.local.entity.Task.class);
                    t.isSynced = true;
                    tasks.add(t);
                }
                AppDatabase.databaseWriteExecutor.execute(() -> localDb.taskDao().insertAll(tasks));
                Log.d(TAG, "Real-time: Synced " + tasks.size() + " tasks.");
            }
        });
        listeners.add(taskListener);

        ListenerRegistration invoiceListener = remoteDb.collection("invoices").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Invoice listen failed.", e);
                return;
            }
            if (snapshots != null) {
                List<Invoice> invoices = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    Invoice i = doc.toObject(Invoice.class);
                    i.isSynced = true;
                    invoices.add(i);
                }
                AppDatabase.databaseWriteExecutor.execute(() -> localDb.invoiceDao().insertAll(invoices));
                Log.d(TAG, "Real-time: Synced " + invoices.size() + " invoices.");
            }
        });
        listeners.add(invoiceListener);
    }

    public void stopRealtimeSync() {
        for (ListenerRegistration listener : listeners) {
            listener.remove();
        }
        listeners.clear();
    }

    private List<User> fetchUsers() throws ExecutionException, InterruptedException {
        Task<List<User>> task = remoteDb.collection("users").get().continueWith(t -> {
            List<User> list = new ArrayList<>();
            if (t.isSuccessful()) {
                for (QueryDocumentSnapshot doc : t.getResult()) {
                    list.add(doc.toObject(User.class));
                }
            }
            return list;
        });
        return Tasks.await(task);
    }

    private List<Project> fetchProjects() throws ExecutionException, InterruptedException {
        Task<List<Project>> task = remoteDb.collection("projects").get().continueWith(t -> {
            List<Project> list = new ArrayList<>();
            if (t.isSuccessful()) {
                for (QueryDocumentSnapshot doc : t.getResult()) {
                    Project p = doc.toObject(Project.class);
                    p.isSynced = true; // Mark as synced from server
                    list.add(p);
                }
            }
            return list;
        });
        return Tasks.await(task);
    }

    private List<DailyReport> fetchDailyReports() throws ExecutionException, InterruptedException {
        Task<List<DailyReport>> task = remoteDb.collection("daily_reports").get().continueWith(t -> {
            List<DailyReport> list = new ArrayList<>();
            if (t.isSuccessful()) {
                for (QueryDocumentSnapshot doc : t.getResult()) {
                    DailyReport r = doc.toObject(DailyReport.class);
                    r.isSynced = true;
                    list.add(r);
                }
            }
            return list;
        });
        return Tasks.await(task);
    }

    private List<Attendance> fetchAttendance(String userId) throws ExecutionException, InterruptedException {
        Task<List<Attendance>> task = remoteDb.collection("attendance")
                .whereEqualTo("userId", userId)
                .get()
                .continueWith(t -> {
                    List<Attendance> list = new ArrayList<>();
                    if (t.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : t.getResult()) {
                            Attendance a = doc.toObject(Attendance.class);
                            a.isSynced = true;
                            list.add(a);
                        }
                    }
                    return list;
                });
        return Tasks.await(task);
    }

    private List<MaterialRequest> fetchMaterialRequests() throws ExecutionException, InterruptedException {
        Task<List<MaterialRequest>> task = remoteDb.collection("material_requests").get().continueWith(t -> {
            List<MaterialRequest> list = new ArrayList<>();
            if (t.isSuccessful()) {
                for (QueryDocumentSnapshot doc : t.getResult()) {
                    MaterialRequest r = doc.toObject(MaterialRequest.class);
                    r.isSynced = true;
                    list.add(r);
                }
            }
            return list;
        });
        return Tasks.await(task);
    }

    private List<com.example.sitepulse.data.local.entity.Task> fetchTasks() throws ExecutionException, InterruptedException {
        Task<List<com.example.sitepulse.data.local.entity.Task>> task = remoteDb.collection("tasks").get().continueWith(t -> {
            List<com.example.sitepulse.data.local.entity.Task> list = new ArrayList<>();
            if (t.isSuccessful()) {
                for (QueryDocumentSnapshot doc : t.getResult()) {
                    com.example.sitepulse.data.local.entity.Task taskItem = doc.toObject(com.example.sitepulse.data.local.entity.Task.class);
                    taskItem.isSynced = true;
                    list.add(taskItem);
                }
            }
            return list;
        });
        return Tasks.await(task);
    }

    private List<Invoice> fetchInvoices() throws ExecutionException, InterruptedException {
        Task<List<Invoice>> task = remoteDb.collection("invoices").get().continueWith(t -> {
            List<Invoice> list = new ArrayList<>();
            if (t.isSuccessful()) {
                for (QueryDocumentSnapshot doc : t.getResult()) {
                    Invoice i = doc.toObject(Invoice.class);
                    i.isSynced = true;
                    list.add(i);
                }
            }
            return list;
        });
        return Tasks.await(task);
    }
}
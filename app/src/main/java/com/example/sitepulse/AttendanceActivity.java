package com.example.sitepulse;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Attendance;
import com.example.sitepulse.data.local.entity.Project;
import com.example.sitepulse.data.repository.SyncRepository;
import com.example.sitepulse.util.LocationHelper;
import com.example.sitepulse.util.NetworkUtils;
import com.example.sitepulse.worker.SyncWorker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AttendanceActivity extends AppCompatActivity {

    private TextView tvCurrentStatus, tvTime, tvLocationStatus;
    private Button btnClockIn, btnClockOut;
    private ProgressBar pbSync;
    private LocationHelper locationHelper;
    private AppDatabase db;
    private FirebaseAuth mAuth;
    private SyncRepository syncRepository;
    private String currentUserId;
    private Attendance currentAttendance;
    private Project currentProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        db = AppDatabase.getDatabase(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        locationHelper = new LocationHelper(this);
        syncRepository = new SyncRepository(db, FirebaseFirestore.getInstance());

        initViews();
        checkPermissions();
        
        String projectId = getIntent().getStringExtra("PROJECT_ID");
        if (projectId != null) {
            loadProject(projectId);
        } else {
            loadAssignedProject();
        }

        if (NetworkUtils.isNetworkAvailable(this)) {
            pbSync.setVisibility(View.VISIBLE);
            syncAttendanceData();
        } 
    }

    private void syncAttendanceData() {
        syncRepository.syncAttendanceForUser(currentUserId, new SyncRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    pbSync.setVisibility(View.GONE);
                    Toast.makeText(AttendanceActivity.this, "Attendance synced", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    pbSync.setVisibility(View.GONE);
                    Toast.makeText(AttendanceActivity.this, "Sync failed, showing local data", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void initViews() {
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        tvTime = findViewById(R.id.tvTime);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        btnClockIn = findViewById(R.id.btnClockIn);
        btnClockOut = findViewById(R.id.btnClockOut);
        pbSync = findViewById(R.id.pbSync);

        btnClockIn.setOnClickListener(v -> handleClockIn());
        btnClockOut.setOnClickListener(v -> handleClockOut());
    }

    private void loadProject(String projectId) {
        db.projectDao().getProjectById(projectId).observe(this, project -> {
            if (project != null) {
                currentProject = project;
                tvLocationStatus.setText("Site: " + currentProject.name);
                observeAttendance();
            } else {
                tvLocationStatus.setText("Project not found");
            }
        });
    }

    private void loadAssignedProject() {
        db.projectDao().getProjectsForEngineer(currentUserId).observe(this, projects -> {
            if (projects != null && !projects.isEmpty()) {
                currentProject = projects.get(0);
                tvLocationStatus.setText("Site: " + currentProject.name);
                observeAttendance();
            } else {
                tvLocationStatus.setText("No Assigned Project");
            }
        });
    }

    private void observeAttendance() {
        if (currentProject == null) return;
        long startOfDay = getStartOfDay();
        db.attendanceDao().getTodayAttendanceForProject(currentUserId, currentProject.id, startOfDay, startOfDay + 86400000)
                .observe(this, this::updateUI);
    }

    private void updateUI(Attendance attendance) {
        currentAttendance = attendance;
        if (attendance == null) {
            tvCurrentStatus.setText("OFF DUTY");
            tvCurrentStatus.setTextColor(Color.RED);
            tvTime.setText("Not clocked in today");
            btnClockIn.setEnabled(true);
            btnClockOut.setEnabled(false);
        } else if (attendance.clockOutTime == 0) {
            tvCurrentStatus.setText("ON DUTY");
            tvCurrentStatus.setTextColor(Color.GREEN);
            String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(attendance.clockInTime));
            tvTime.setText("Clocked in at: " + time);
            btnClockIn.setEnabled(false);
            btnClockOut.setEnabled(true);
        } else {
            tvCurrentStatus.setText("COMPLETED");
            tvCurrentStatus.setTextColor(Color.BLUE);
            String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(attendance.clockOutTime));
            tvTime.setText("Clocked out at: " + time);
            btnClockIn.setEnabled(false);
            btnClockOut.setEnabled(false);
        }
    }

    private void handleClockIn() {
        if (!isLocationEnabled()) {
            promptToEnableLocation();
            return;
        }
        if (currentProject == null) {
            Toast.makeText(this, "No project assigned. Cannot clock in.", Toast.LENGTH_LONG).show();
            return;
        }
        tvLocationStatus.setText("Verifying Location...");
        btnClockIn.setEnabled(false);
        locationHelper.getCurrentLocation(location -> {
            if (location != null) {
                checkGeofenceAndClockIn(location);
            } else {
                Toast.makeText(this, "Failed to get location. Try again.", Toast.LENGTH_SHORT).show();
                btnClockIn.setEnabled(true);
            }
        }, e -> {
            Toast.makeText(this, "Location Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnClockIn.setEnabled(true);
        });
    }

    private void checkGeofenceAndClockIn(Location userLocation) {
        boolean isOnSite = LocationHelper.isUserOnSite(
                userLocation.getLatitude(), userLocation.getLongitude(),
                currentProject.latitude, currentProject.longitude, currentProject.radiusMeters
        );
        if (isOnSite) {
            saveClockIn(userLocation);
        } else {
            float distance = LocationHelper.getDistanceInMeters(userLocation.getLatitude(), userLocation.getLongitude(), currentProject.latitude, currentProject.longitude);
            tvLocationStatus.setText("You are " + (int)distance + "m away. Required: " + (int)currentProject.radiusMeters + "m");
            Toast.makeText(this, "You must be on site to clock in!", Toast.LENGTH_LONG).show();
            btnClockIn.setEnabled(true);
        }
    }

    private void saveClockIn(Location location) {
        Attendance attendance = new Attendance(
                UUID.randomUUID().toString(),
                currentUserId,
                currentProject.id,
                System.currentTimeMillis(),
                0,
                "PRESENT",
                location.getLatitude(),
                location.getLongitude()
        );
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.attendanceDao().insert(attendance);
            triggerSync();
        });
    }

    private void handleClockOut() {
        if (currentAttendance == null) return;
        currentAttendance.clockOutTime = System.currentTimeMillis();
        currentAttendance.isSynced = false;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.attendanceDao().update(currentAttendance);
            triggerSync();
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

    private long getStartOfDay() {
        long now = System.currentTimeMillis();
        long offset = java.util.TimeZone.getDefault().getOffset(now);
        return ((now + offset) / 86400000) * 86400000 - offset;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void promptToEnableLocation() {
        new AlertDialog.Builder(this)
                .setTitle("Location Services Required")
                .setMessage("This feature requires location services to be enabled. Please enable them in your device settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    // Permission granted
                } else {
                    Toast.makeText(this, "Location permission required for attendance", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }
}
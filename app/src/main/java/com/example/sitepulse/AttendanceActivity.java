package com.example.sitepulse;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Attendance;
import com.example.sitepulse.data.local.entity.Project;
import com.example.sitepulse.util.LocationHelper;
import com.example.sitepulse.worker.SyncWorker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AttendanceActivity extends AppCompatActivity {

    private TextView tvCurrentStatus, tvTime, tvLocationStatus;
    private Button btnClockIn, btnClockOut;
    private LocationHelper locationHelper;
    private AppDatabase db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private Attendance currentAttendance;
    
    // Test Project Location (Can be replaced by DB fetch)
    // Defaulting to 0,0 - will update with first found project
    private double projectLat = 0.0;
    private double projectLng = 0.0;
    private double projectRadius = 100.0; // 100 meters
    private String projectId = "project_1"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        db = AppDatabase.getDatabase(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        locationHelper = new LocationHelper(this);

        initViews();
        checkPermissions();
        loadProjectLocation();
        observeAttendance();
    }

    private void initViews() {
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        tvTime = findViewById(R.id.tvTime);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        btnClockIn = findViewById(R.id.btnClockIn);
        btnClockOut = findViewById(R.id.btnClockOut);

        btnClockIn.setOnClickListener(v -> handleClockIn());
        btnClockOut.setOnClickListener(v -> handleClockOut());
    }

    private void loadProjectLocation() {
        // Fetch the first project to use its location
        // In a real app, user would select the project or be assigned one
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Project project = db.projectDao().getFirstProject(); // Need to add this method to DAO or just list all
             // For now, let's just use the query we have
             // We'll fetch all and take the first one
        });
        
        // Temporarily fetching all projects to get one
        db.projectDao().getAllProjects().observe(this, projects -> {
            if (projects != null && !projects.isEmpty()) {
                Project p = projects.get(0);
                projectId = p.id;
                projectLat = p.latitude;
                projectLng = p.longitude;
                projectRadius = p.radiusMeters > 0 ? p.radiusMeters : 100.0;
                tvLocationStatus.setText("Site: " + p.name + " (" + projectLat + ", " + projectLng + ")");
            } else {
                tvLocationStatus.setText("No Projects Found. Please create a project.");
            }
        });
    }

    private void observeAttendance() {
        long startOfDay = getStartOfDay();
        long endOfDay = startOfDay + 86400000; // + 24 hours

        db.attendanceDao().getTodayAttendance(currentUserId, startOfDay, endOfDay)
                .observe(this, attendance -> {
                    currentAttendance = attendance;
                    updateUI(attendance);
                });
    }

    private void updateUI(Attendance attendance) {
        if (attendance == null) {
            // No record for today -> Ready to Clock In
            tvCurrentStatus.setText("OFF DUTY");
            tvCurrentStatus.setTextColor(Color.RED);
            tvTime.setText("Not clocked in today");
            btnClockIn.setEnabled(true);
            btnClockOut.setEnabled(false);
            btnClockIn.setAlpha(1.0f);
            btnClockOut.setAlpha(0.5f);
        } else if (attendance.clockOutTime == 0) {
            // Clocked In, currently working
            tvCurrentStatus.setText("ON DUTY");
            tvCurrentStatus.setTextColor(Color.GREEN);
            String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(attendance.clockInTime));
            tvTime.setText("Clocked in at: " + time);
            btnClockIn.setEnabled(false);
            btnClockOut.setEnabled(true);
            btnClockIn.setAlpha(0.5f);
            btnClockOut.setAlpha(1.0f);
        } else {
            // Already Clocked Out for the day
            tvCurrentStatus.setText("COMPLETED");
            tvCurrentStatus.setTextColor(Color.BLUE);
            String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(attendance.clockOutTime));
            tvTime.setText("Clocked out at: " + time);
            btnClockIn.setEnabled(false); // Can't clock in again same day for this simplified logic
            btnClockOut.setEnabled(false);
            btnClockIn.setAlpha(0.5f);
            btnClockOut.setAlpha(0.5f);
        }
    }

    private void handleClockIn() {
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
                projectLat, projectLng, projectRadius
        );

        if (isOnSite) {
            // Save Attendance
            saveClockIn(userLocation);
        } else {
            float distance = LocationHelper.getDistanceInMeters(userLocation.getLatitude(), userLocation.getLongitude(), projectLat, projectLng);
            tvLocationStatus.setText("You are " + (int)distance + "m away from site. Max allowed: " + (int)projectRadius + "m");
            Toast.makeText(this, "You must be on site to clock in!", Toast.LENGTH_LONG).show();
            btnClockIn.setEnabled(true);
        }
    }

    private void saveClockIn(Location location) {
        Attendance attendance = new Attendance(
                UUID.randomUUID().toString(),
                currentUserId,
                projectId,
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
        currentAttendance.isSynced = false; // Mark unsynced to push update

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
        // Simplified start of day calculation
        long now = System.currentTimeMillis();
        long offset = java.util.TimeZone.getDefault().getOffset(now);
        return ((now + offset) / 86400000) * 86400000 - offset;
    }

    // Permission Handling
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    // Precise location access granted.
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    // Only approximate location access granted.
                } else {
                    // No location access granted.
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
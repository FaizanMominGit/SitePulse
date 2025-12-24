package com.example.sitepulse;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.DailyReport;
import com.example.sitepulse.util.ImageUtils;
import com.example.sitepulse.worker.SyncWorker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CreateDprActivity extends AppCompatActivity {

    private TextInputEditText etLaborCount, etWorkDescription, etHindrances;
    private Button btnCapturePhoto, btnSaveReport;
    private ImageView ivSitePhoto;
    
    private AppDatabase db;
    private FirebaseAuth mAuth;
    private String projectId;
    private String currentUserId;
    private String currentImagePath = null;
    
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_dpr);

        if (getIntent().hasExtra("PROJECT_ID")) {
            projectId = getIntent().getStringExtra("PROJECT_ID");
        } else {
            Toast.makeText(this, "Error: No Project ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getDatabase(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "unknown";

        initViews();
    }

    private void initViews() {
        etLaborCount = findViewById(R.id.etLaborCount);
        etWorkDescription = findViewById(R.id.etWorkDescription);
        etHindrances = findViewById(R.id.etHindrances);
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto);
        btnSaveReport = findViewById(R.id.btnSaveReport);
        ivSitePhoto = findViewById(R.id.ivSitePhoto);

        btnCapturePhoto.setOnClickListener(v -> checkCameraPermission());
        btnSaveReport.setOnClickListener(v -> saveReport());
    }

    // Permission Launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "Camera permission required to take photos", Toast.LENGTH_LONG).show();
                }
            });

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success) {
                    ivSitePhoto.setImageURI(photoUri);
                    // currentImagePath is already set in createImageFile()
                } else {
                    currentImagePath = null;
                    Toast.makeText(this, "Photo capture failed", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void dispatchTakePictureIntent() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e("Camera", "Error creating file", ex);
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            try {
                photoUri = FileProvider.getUriForFile(this,
                        "com.example.sitepulse.fileprovider",
                        photoFile);
                takePictureLauncher.launch(photoUri);
            } catch (Exception e) {
                Log.e("Camera", "Error launching camera", e);
                Toast.makeText(this, "Error starting camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentImagePath = image.getAbsolutePath();
        return image;
    }

    private void saveReport() {
        String laborCountStr = etLaborCount.getText().toString().trim();
        String workDesc = etWorkDescription.getText().toString().trim();
        String hindrances = etHindrances.getText().toString().trim();

        if (TextUtils.isEmpty(laborCountStr)) {
            etLaborCount.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(workDesc)) {
            etWorkDescription.setError("Required");
            return;
        }

        // Show progress (optional but good UI practice)
        btnSaveReport.setEnabled(false);
        btnSaveReport.setText("Saving...");

        int laborCount = Integer.parseInt(laborCountStr);
        long timestamp = System.currentTimeMillis();
        String reportId = UUID.randomUUID().toString();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            String finalImagePath = currentImagePath;
            
            // Compress Image if exists
            if (currentImagePath != null) {
                try {
                    Uri originalUri = Uri.fromFile(new File(currentImagePath));
                    Uri compressedUri = ImageUtils.compressImage(CreateDprActivity.this, originalUri);
                    if (compressedUri != null) {
                        finalImagePath = compressedUri.getPath();
                    }
                } catch (Exception e) {
                    Log.e("CreateDpr", "Compression failed, using original", e);
                }
            }

            DailyReport report = new DailyReport(
                    reportId,
                    projectId,
                    currentUserId,
                    timestamp,
                    laborCount,
                    workDesc,
                    hindrances,
                    finalImagePath
            );

            db.dailyReportDao().insert(report);
            
            triggerSync();

            runOnUiThread(() -> {
                Toast.makeText(this, "Report Saved Successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
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
}
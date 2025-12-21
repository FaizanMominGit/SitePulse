package com.example.sitepulse;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.MaterialRequest;
import com.example.sitepulse.worker.SyncWorker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.UUID;

public class CreateMaterialActivity extends AppCompatActivity {

    private TextInputEditText etItemName, etQuantity, etUnit;
    private RadioGroup rgUrgency;
    private Button btnSubmitRequest;

    private AppDatabase db;
    private FirebaseAuth mAuth;
    private String projectId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_material);

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
        etItemName = findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        etUnit = findViewById(R.id.etUnit);
        rgUrgency = findViewById(R.id.rgUrgency);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);

        btnSubmitRequest.setOnClickListener(v -> saveRequest());
    }

    private void saveRequest() {
        String itemName = etItemName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();

        if (TextUtils.isEmpty(itemName)) {
            etItemName.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(unit)) {
            etUnit.setError("Required");
            return;
        }

        double quantity = Double.parseDouble(quantityStr);
        
        String urgency = "Medium";
        int selectedId = rgUrgency.getCheckedRadioButtonId();
        if (selectedId == R.id.rbLow) urgency = "Low";
        else if (selectedId == R.id.rbHigh) urgency = "High";

        long timestamp = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        MaterialRequest request = new MaterialRequest(
                requestId,
                projectId,
                currentUserId,
                itemName,
                quantity,
                unit,
                urgency,
                "PENDING",
                timestamp
        );

        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.materialRequestDao().insert(request);
            triggerSync();
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Request Submitted", Toast.LENGTH_SHORT).show();
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
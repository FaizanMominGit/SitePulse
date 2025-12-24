package com.example.sitepulse;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etRole;
    private Button btnSaveProfile, btnLogout, btnTestCrash;
    private TextView tvAppVersion;
    
    private AppDatabase db;
    private FirebaseAuth mAuth;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = AppDatabase.getDatabase(this);
        mAuth = FirebaseAuth.getInstance();

        initViews();
        loadUserData();
        setAppVersion();

        btnSaveProfile.setOnClickListener(v -> saveProfile());
        
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Test Crash Button - For Development Only
        // Set visibility to VISIBLE to test, then back to GONE
        btnTestCrash.setVisibility(View.GONE); 
        btnTestCrash.setOnClickListener(v -> {
            throw new RuntimeException("Test Crash for SitePulse");
        });
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etRole = findViewById(R.id.etRole);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnTestCrash = findViewById(R.id.btnTestCrash);
        tvAppVersion = findViewById(R.id.tvAppVersion);
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            // Observer for local DB changes
            db.userDao().getUser(firebaseUser.getUid()).observe(this, user -> {
                if (user != null) {
                    currentUser = user;
                    updateUI(user);
                } else {
                    // If local DB is empty, fetch from Firestore
                    fetchFromFirestore(firebaseUser.getUid());
                }
            });
        }
    }

    private void fetchFromFirestore(String uid) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Save to local DB, which will trigger the observer above
                            AppDatabase.databaseWriteExecutor.execute(() -> db.userDao().insert(user));
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void updateUI(User user) {
        etName.setText(user.name);
        etEmail.setText(user.email);
        etRole.setText(user.role);
        etPhone.setText(user.phoneNumber);
    }

    private void saveProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "User data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = etName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        if (newName.isEmpty()) {
            etName.setError("Name required");
            return;
        }

        currentUser.name = newName;
        currentUser.phoneNumber = newPhone;

        // Update Local
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.userDao().insert(currentUser);
            
            // Update Remote
            FirebaseFirestore.getInstance().collection("users")
                    .document(currentUser.id)
                    .update("name", newName, "phoneNumber", newPhone)
                    .addOnSuccessListener(aVoid -> runOnUiThread(() -> 
                        Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show()))
                    .addOnFailureListener(e -> runOnUiThread(() -> 
                        Toast.makeText(ProfileActivity.this, "Failed to sync changes", Toast.LENGTH_SHORT).show()));
        });
    }

    private void setAppVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            tvAppVersion.setText("Version " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
package com.example.sitepulse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.repository.SyncRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup, tvForgotPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SyncRepository syncRepository;
    private AppDatabase localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        localDb = AppDatabase.getDatabase(this);
        syncRepository = new SyncRepository(localDb, db);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            redirectUserByRole(currentUser.getUid());
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> loginUser());
        tvSignup.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            progressDialog.setMessage("Syncing data...");
                            syncRepository.syncAllData(user.getUid(), new SyncRepository.SyncCallback() {
                                @Override
                                public void onSuccess() {
                                    // Start real-time sync after initial sync succeeds
                                    syncRepository.startRealtimeSync();
                                    
                                    runOnUiThread(() -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "Sync successful", Toast.LENGTH_SHORT).show();
                                        redirectUserByRole(user.getUid());
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    runOnUiThread(() -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "Sync failed, showing local data.", Toast.LENGTH_LONG).show();
                                        redirectUserByRole(user.getUid());
                                    });
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Please verify your email address first.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Authentication Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectUserByRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("Manager".equalsIgnoreCase(role)) {
                            startActivity(new Intent(LoginActivity.this, ManagerDashboardActivity.class));
                        } else {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Error fetching user role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                });
    }
}
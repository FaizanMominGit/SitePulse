package com.example.sitepulse;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnSignup;
    private TextView tvLoginRedirect;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AppDatabase localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        localDb = AppDatabase.getDatabase(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmailSignup);
        etPassword = findViewById(R.id.etPasswordSignup);
        btnSignup = findViewById(R.id.btnSignup);
        tvLoginRedirect = findViewById(R.id.tvLoginRedirect);

        btnSignup.setOnClickListener(v -> createUser());

        tvLoginRedirect.setOnClickListener(v -> finish());
    }

    private void createUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Send verification email
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            // Proceed to save data
                                            saveUserToFirestoreAndLocal(firebaseUser.getUid(), name, email);
                                        } else {
                                            Toast.makeText(SignupActivity.this, 
                                                "Failed to send verification email: " + emailTask.getException().getMessage(), 
                                                Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SignupActivity.this, "Authentication Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestoreAndLocal(String uid, String name, String email) {
        // 1. Prepare data for Firestore
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", uid);
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("role", "Engineer"); // Default role

        // 2. Save to Firestore
        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    // 3. Save to Local Database (Room) on background thread
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        User localUser = new User(uid, name, email, "Engineer");
                        localDb.userDao().insert(localUser);

                        // 4. Sign out and redirect to Login
                        runOnUiThread(() -> {
                            mAuth.signOut(); // Ensure user cannot access app until verified
                            Toast.makeText(SignupActivity.this, 
                                "Registration successful! Please verify your email.", 
                                Toast.LENGTH_LONG).show();
                            
                            // Go back to Login Activity
                            finish();
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
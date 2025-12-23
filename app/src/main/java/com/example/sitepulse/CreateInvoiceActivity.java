package com.example.sitepulse;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Invoice;
import com.example.sitepulse.worker.SyncWorker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

public class CreateInvoiceActivity extends AppCompatActivity {

    private TextInputEditText etInvoiceNumber, etClientName, etDescription, etSubtotal, etGstRate;
    private TextView tvTotalAmount;
    private Button btnSaveInvoice;
    private AppDatabase db;
    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice);

        db = AppDatabase.getDatabase(this);

        if (getIntent().hasExtra("PROJECT_ID")) {
            projectId = getIntent().getStringExtra("PROJECT_ID");
        } else {
            Toast.makeText(this, "Project ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etInvoiceNumber = findViewById(R.id.etInvoiceNumber);
        etClientName = findViewById(R.id.etClientName);
        etDescription = findViewById(R.id.etDescription);
        etSubtotal = findViewById(R.id.etSubtotal);
        etGstRate = findViewById(R.id.etGstRate);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnSaveInvoice = findViewById(R.id.btnSaveInvoice);

        btnSaveInvoice.setOnClickListener(v -> saveInvoice());
    }

    private void setupListeners() {
        TextWatcher calculatorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateTotal();
            }
        };

        etSubtotal.addTextChangedListener(calculatorWatcher);
        etGstRate.addTextChangedListener(calculatorWatcher);
    }

    private void calculateTotal() {
        double subtotal = parseDouble(etSubtotal.getText().toString());
        double gstRate = parseDouble(etGstRate.getText().toString());
        
        double gstAmount = subtotal * (gstRate / 100);
        double total = subtotal + gstAmount;

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        tvTotalAmount.setText(format.format(total));
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void saveInvoice() {
        String invoiceNumber = etInvoiceNumber.getText().toString().trim();
        String clientName = etClientName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        double subtotal = parseDouble(etSubtotal.getText().toString());
        double gstRate = parseDouble(etGstRate.getText().toString());

        if (invoiceNumber.isEmpty()) {
            etInvoiceNumber.setError("Required");
            return;
        }
        if (clientName.isEmpty()) {
            etClientName.setError("Required");
            return;
        }
        if (subtotal <= 0) {
            etSubtotal.setError("Invalid amount");
            return;
        }

        double gstAmount = subtotal * (gstRate / 100);
        double totalAmount = subtotal + gstAmount;

        Invoice invoice = new Invoice(
                UUID.randomUUID().toString(),
                projectId,
                invoiceNumber,
                clientName,
                description,
                subtotal,
                gstRate,
                gstAmount,
                totalAmount,
                "DRAFT", // Default status
                System.currentTimeMillis()
        );

        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.invoiceDao().insertAll(Collections.singletonList(invoice));
            runOnUiThread(() -> {
                Toast.makeText(this, "Invoice created", Toast.LENGTH_SHORT).show();
                triggerImmediateSync();
                finish();
            });
        });
    }

    private void triggerImmediateSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest =
                new OneTimeWorkRequest.Builder(SyncWorker.class)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueue(syncRequest);
    }
}

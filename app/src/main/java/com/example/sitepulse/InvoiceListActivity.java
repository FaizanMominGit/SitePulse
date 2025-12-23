package com.example.sitepulse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Project;
import com.example.sitepulse.data.repository.SyncRepository;
import com.example.sitepulse.ui.adapter.InvoiceAdapter;
import com.example.sitepulse.util.NetworkUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class InvoiceListActivity extends AppCompatActivity {

    private RecyclerView rvInvoices;
    private FloatingActionButton fabCreateInvoice;
    private InvoiceAdapter adapter;
    private AppDatabase db;
    private SyncRepository syncRepository;
    private String currentProjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_list);

        db = AppDatabase.getDatabase(this);
        syncRepository = new SyncRepository(db, FirebaseFirestore.getInstance());

        initViews();
        setupRecyclerView();

        if (getIntent().hasExtra("PROJECT_ID")) {
            currentProjectId = getIntent().getStringExtra("PROJECT_ID");
            loadInvoices();
        } else {
            loadCurrentProjectId();
        }

        if (NetworkUtils.isNetworkAvailable(this)) {
            syncRepository.syncInvoices(new SyncRepository.SyncCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(InvoiceListActivity.this, "Invoices synced", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(InvoiceListActivity.this, "Sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    private void loadCurrentProjectId() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Project p = db.projectDao().getFirstProject();
            if (p != null) {
                currentProjectId = p.id;
                runOnUiThread(this::loadInvoices);
            } else {
                runOnUiThread(() -> Toast.makeText(this, "No Project Found", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void initViews() {
        rvInvoices = findViewById(R.id.rvInvoices);
        fabCreateInvoice = findViewById(R.id.fabCreateInvoice);

        fabCreateInvoice.setOnClickListener(v -> {
            if (currentProjectId != null) {
                Intent intent = new Intent(InvoiceListActivity.this, CreateInvoiceActivity.class);
                intent.putExtra("PROJECT_ID", currentProjectId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No Project Selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new InvoiceAdapter();
        rvInvoices.setLayoutManager(new LinearLayoutManager(this));
        rvInvoices.setAdapter(adapter);

        adapter.setOnInvoiceClickListener(invoice -> {
            // Future: Show detailed invoice view
            Toast.makeText(this, "Clicked Invoice #" + invoice.invoiceNumber, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadInvoices() {
        if (currentProjectId == null) return;

        db.invoiceDao().getInvoicesForProject(currentProjectId).observe(this, invoices -> {
            adapter.setInvoices(invoices);
        });
    }
}
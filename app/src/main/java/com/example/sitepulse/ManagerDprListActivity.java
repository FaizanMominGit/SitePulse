package com.example.sitepulse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.DailyReport;
import com.example.sitepulse.data.repository.SyncRepository;
import com.example.sitepulse.ui.adapter.DprAdapter;
import com.example.sitepulse.util.NetworkUtils;
import com.google.firebase.firestore.FirebaseFirestore;

public class ManagerDprListActivity extends AppCompatActivity {

    private RecyclerView rvDprList;
    private DprAdapter dprAdapter;
    private AppDatabase db;
    private SyncRepository syncRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dpr_list);

        db = AppDatabase.getDatabase(this);
        syncRepository = new SyncRepository(db, FirebaseFirestore.getInstance());

        initViews();
        setupRecyclerView();
        
        if (NetworkUtils.isNetworkAvailable(this)) {
            syncRepository.syncDprs(new SyncRepository.SyncCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(ManagerDprListActivity.this, "Reports synced", Toast.LENGTH_SHORT).show();
                        loadAllReports();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(ManagerDprListActivity.this, "Sync failed", Toast.LENGTH_SHORT).show();
                        loadAllReports();
                    });
                }
            });
        } else {
            loadAllReports();
        }
    }

    private void initViews() {
        rvDprList = findViewById(R.id.rvDprList);
    }

    private void setupRecyclerView() {
        dprAdapter = new DprAdapter();
        rvDprList.setLayoutManager(new LinearLayoutManager(this));
        rvDprList.setAdapter(dprAdapter);

        dprAdapter.setOnDprClickListener(report -> {
            Intent intent = new Intent(ManagerDprListActivity.this, DprDetailActivity.class);
            intent.putExtra("REPORT_ID", report.id);
            startActivity(intent);
        });
    }

    private void loadAllReports() {
        db.dailyReportDao().getAllReports().observe(this, reports -> {
            dprAdapter.setReports(reports);
        });
    }
}
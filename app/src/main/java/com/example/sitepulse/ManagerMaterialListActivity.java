package com.example.sitepulse;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.MaterialRequest;
import com.example.sitepulse.data.repository.SyncRepository;
import com.example.sitepulse.ui.adapter.ManagerMaterialAdapter;
import com.example.sitepulse.ui.viewmodel.MaterialRequestViewModel;
import com.example.sitepulse.util.NetworkUtils;
import com.google.firebase.firestore.FirebaseFirestore;

public class ManagerMaterialListActivity extends AppCompatActivity implements ManagerMaterialAdapter.OnActionClickListener {

    private RecyclerView rvMaterialRequests;
    private ManagerMaterialAdapter adapter;
    private MaterialRequestViewModel viewModel;
    private SyncRepository syncRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_material_list);

        AppDatabase db = AppDatabase.getDatabase(this);
        syncRepository = new SyncRepository(db, FirebaseFirestore.getInstance());

        initViews();
        setupRecyclerView();
        setupViewModel();

        if (NetworkUtils.isNetworkAvailable(this)) {
            syncRepository.syncMaterialRequests(new SyncRepository.SyncCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> Toast.makeText(ManagerMaterialListActivity.this, "Material requests synced", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> Toast.makeText(ManagerMaterialListActivity.this, "Sync failed", Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private void initViews() {
        rvMaterialRequests = findViewById(R.id.rvMaterialRequests);
    }

    private void setupRecyclerView() {
        adapter = new ManagerMaterialAdapter();
        adapter.setOnActionClickListener(this);
        rvMaterialRequests.setLayoutManager(new LinearLayoutManager(this));
        rvMaterialRequests.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MaterialRequestViewModel.class);
        viewModel.getAllMaterialRequests().observe(this, materialRequests -> {
            adapter.setMaterialRequests(materialRequests);
        });
    }

    @Override
    public void onApproveClick(MaterialRequest request) {
        updateRequestStatus(request, "APPROVED");
    }

    @Override
    public void onRejectClick(MaterialRequest request) {
        updateRequestStatus(request, "REJECTED");
    }

    private void updateRequestStatus(MaterialRequest request, String status) {
        syncRepository.updateRequestStatus(request, status, new SyncRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> Toast.makeText(ManagerMaterialListActivity.this, "Request " + status.toLowerCase(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> Toast.makeText(ManagerMaterialListActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
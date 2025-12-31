package com.example.sitepulse;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.DailyReport;
import com.example.sitepulse.data.local.entity.Project;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManagerDashboardActivity extends AppCompatActivity {

    private Button btnManageProjects, btnViewReports, btnMaterialRequests, btnInvoices;
    private ImageButton btnProfile;
    
    // Analytics UI
    private AutoCompleteTextView spinnerProject;
    private TextView tvTotalInvoiced, tvPendingTasks;
    private LineChart chartLabor;
    private PieChart chartTasks;
    
    private AppDatabase db;
    private FirebaseAuth mAuth;
    private List<Project> allProjects = new ArrayList<>();
    private Project selectedProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        db = AppDatabase.getDatabase(this);
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupCharts();
        loadProjects();

        // Save FCM token for manager notifications
        fetchAndSaveFcmToken();
    }

    private void fetchAndSaveFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM_DEBUG", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && token != null) {
                        // Update in local DB
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            db.userDao().updateFcmToken(user.getUid(), token);
                        });
                        // Update in Firestore
                        FirebaseFirestore.getInstance().collection("users")
                                .document(user.getUid())
                                .update("fcmToken", token)
                                .addOnSuccessListener(aVoid -> Log.d("FCM_DEBUG", "Manager FCM Token saved to Firestore."))
                                .addOnFailureListener(e -> Log.e("FCM_DEBUG", "Failed to save manager FCM Token to Firestore.", e));
                    }
                });
    }

    private void initViews() {
        btnManageProjects = findViewById(R.id.btnManageProjects);
        btnViewReports = findViewById(R.id.btnViewReports);
        btnMaterialRequests = findViewById(R.id.btnMaterialRequests);
        btnInvoices = findViewById(R.id.btnInvoices);
        btnProfile = findViewById(R.id.btnProfile);
        
        spinnerProject = findViewById(R.id.spinnerProject);
        tvTotalInvoiced = findViewById(R.id.tvTotalInvoiced);
        tvPendingTasks = findViewById(R.id.tvPendingTasks);
        chartLabor = findViewById(R.id.chartLabor);
        chartTasks = findViewById(R.id.chartTasks);

        btnManageProjects.setOnClickListener(v -> startActivity(new Intent(ManagerDashboardActivity.this, ManagerProjectListActivity.class)));
        btnViewReports.setOnClickListener(v -> startActivity(new Intent(ManagerDashboardActivity.this, ManagerDprListActivity.class)));
        btnMaterialRequests.setOnClickListener(v -> startActivity(new Intent(ManagerDashboardActivity.this, ManagerMaterialListActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(ManagerDashboardActivity.this, ProfileActivity.class)));
        
        btnInvoices.setOnClickListener(v -> {
            if (selectedProject != null) {
                Intent intent = new Intent(ManagerDashboardActivity.this, InvoiceListActivity.class);
                intent.putExtra("PROJECT_ID", selectedProject.id);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select a project first", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupCharts() {
        // Line Chart Setup
        chartLabor.getDescription().setEnabled(false);
        chartLabor.setTouchEnabled(true);
        chartLabor.setDragEnabled(true);
        chartLabor.setScaleEnabled(true);
        chartLabor.setPinchZoom(true);
        chartLabor.setDrawGridBackground(false);
        
        XAxis xAxis = chartLabor.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        
        chartLabor.getAxisLeft().setDrawGridLines(true);
        chartLabor.getAxisRight().setEnabled(false);
        chartLabor.getLegend().setEnabled(false);

        // Pie Chart Setup
        chartTasks.setUsePercentValues(true);
        chartTasks.getDescription().setEnabled(false);
        chartTasks.setExtraOffsets(5, 10, 5, 5);
        chartTasks.setDragDecelerationFrictionCoef(0.95f);
        chartTasks.setDrawHoleEnabled(true);
        chartTasks.setHoleColor(Color.WHITE);
        chartTasks.setTransparentCircleRadius(61f);
    }

    private void loadProjects() {
        db.projectDao().getAllProjects().observe(this, projects -> {
            if (projects != null && !projects.isEmpty()) {
                allProjects = projects;
                setupProjectSpinner();
            }
        });
    }

    private void setupProjectSpinner() {
        List<String> projectNames = new ArrayList<>();
        for (Project p : allProjects) {
            projectNames.add(p.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, projectNames);
        spinnerProject.setAdapter(adapter);

        spinnerProject.setOnItemClickListener((parent, view, position, id) -> {
            selectedProject = allProjects.get(position);
            updateDashboard(selectedProject.id);
        });

        // Default to first project
        if (!allProjects.isEmpty()) {
            spinnerProject.setText(allProjects.get(0).name, false);
            selectedProject = allProjects.get(0);
            updateDashboard(selectedProject.id);
        }
    }

    private void updateDashboard(String projectId) {
        loadTotalInvoiced(projectId);
        loadPendingTasks(projectId);
        loadLaborTrend(projectId);
        loadTaskProgress(projectId);
    }

    private void loadTotalInvoiced(String projectId) {
        db.invoiceDao().getTotalInvoicedAmount(projectId).observe(this, total -> {
            double amount = total != null ? total : 0.0;
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            tvTotalInvoiced.setText(format.format(amount));
        });
    }

    private void loadPendingTasks(String projectId) {
        db.taskDao().getPendingTaskCount(projectId).observe(this, count -> {
            int pending = count != null ? count : 0;
            tvPendingTasks.setText(String.valueOf(pending));
        });
    }

    private void loadLaborTrend(String projectId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<DailyReport> reports = db.dailyReportDao().getLast7Reports(projectId);
            
            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            
            for (int i = 0; i < reports.size(); i++) {
                DailyReport r = reports.get(i);
                entries.add(new Entry(i, r.laborCount));
                labels.add(sdf.format(new Date(r.date)));
            }

            runOnUiThread(() -> {
                if (entries.isEmpty()) {
                    chartLabor.clear();
                    chartLabor.invalidate();
                    return;
                }

                LineDataSet set = new LineDataSet(entries, "Labor Count");
                set.setColor(Color.BLUE);
                set.setLineWidth(2f);
                set.setCircleColor(Color.BLUE);
                set.setCircleRadius(4f);
                set.setDrawValues(false);
                set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

                LineData data = new LineData(set);
                chartLabor.setData(data);
                chartLabor.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                chartLabor.invalidate();
            });
        });
    }

    private void loadTaskProgress(String projectId) {
        db.taskDao().getCompletedTaskCount(projectId).observe(this, completed -> {
            db.taskDao().getPendingTaskCount(projectId).observe(this, pending -> {
                updatePieChart(completed != null ? completed : 0, pending != null ? pending : 0);
            });
        });
    }
    
    private void updatePieChart(int completed, int pending) {
        if (completed == 0 && pending == 0) {
            chartTasks.clear();
            chartTasks.invalidate();
            return;
        }
        
        List<PieEntry> entries = new ArrayList<>();
        if (completed > 0) entries.add(new PieEntry(completed, "Completed"));
        if (pending > 0) entries.add(new PieEntry(pending, "Pending"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        chartTasks.setData(data);
        chartTasks.invalidate();
    }
}
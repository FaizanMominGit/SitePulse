package com.example.sitepulse;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManagerDashboardActivity extends AppCompatActivity {

    private Spinner spinnerProjects;
    private TextView tvTotalInvoiced, tvTotalMaterialCost;
    private LineChart chartLabor;
    private PieChart chartTasks;
    private Button btnProjects, btnDPRs, btnMaterials;
    
    private AppDatabase db;
    private List<Project> projects = new ArrayList<>();
    private String currentProjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        db = AppDatabase.getDatabase(this);

        initViews();
        setupListeners();
        loadProjects();
    }

    private void initViews() {
        spinnerProjects = findViewById(R.id.spinnerProjects);
        tvTotalInvoiced = findViewById(R.id.tvTotalInvoiced);
        tvTotalMaterialCost = findViewById(R.id.tvTotalMaterialCost);
        chartLabor = findViewById(R.id.chartLabor);
        chartTasks = findViewById(R.id.chartTasks);
        btnProjects = findViewById(R.id.btnProjects);
        btnDPRs = findViewById(R.id.btnDPRs);
        btnMaterials = findViewById(R.id.btnMaterials);
    }

    private void setupListeners() {
        btnProjects.setOnClickListener(v -> startActivity(new Intent(this, ManagerProjectListActivity.class)));
        btnDPRs.setOnClickListener(v -> startActivity(new Intent(this, ManagerDprListActivity.class)));
        btnMaterials.setOnClickListener(v -> startActivity(new Intent(this, ManagerMaterialListActivity.class)));

        spinnerProjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!projects.isEmpty()) {
                    currentProjectId = projects.get(position).id;
                    refreshDashboard();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadProjects() {
        db.projectDao().getAllProjects().observe(this, projectList -> {
            projects = projectList;
            List<String> projectNames = new ArrayList<>();
            for (Project p : projects) {
                projectNames.add(p.name);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, projectNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProjects.setAdapter(adapter);

            if (!projects.isEmpty()) {
                currentProjectId = projects.get(0).id;
                refreshDashboard();
            }
        });
    }

    private void refreshDashboard() {
        if (currentProjectId == null) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Financials
            Double invoiced = db.invoiceDao().getTotalInvoicedAmount(currentProjectId);
            Double materialCost = db.materialRequestDao().getTotalEstimatedCost(currentProjectId);

            // 2. Labor Trend
            List<DailyReport> reports = db.dailyReportDao().getLast7Reports(currentProjectId);
            
            // 3. Task Status
            // Use boolean completion status since 'status' field doesn't exist
            List<Boolean> completions = db.taskDao().getTaskCompletionStatus(currentProjectId);
            List<String> statuses = new ArrayList<>();
            for (Boolean isCompleted : completions) {
                statuses.add(isCompleted ? "Completed" : "Pending");
            }

            runOnUiThread(() -> {
                updateFinancials(invoiced != null ? invoiced : 0.0, materialCost != null ? materialCost : 0.0);
                updateLaborChart(reports);
                updateTaskChart(statuses);
            });
        });
    }

    private void updateFinancials(double invoiced, double materialCost) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        tvTotalInvoiced.setText(format.format(invoiced));
        tvTotalMaterialCost.setText(format.format(materialCost));
    }

    private void updateLaborChart(List<DailyReport> reports) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.US);

        for (int i = 0; i < reports.size(); i++) {
            entries.add(new Entry(i, reports.get(i).laborCount));
            labels.add(sdf.format(new Date(reports.get(i).date)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Labor Count");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);

        LineData lineData = new LineData(dataSet);
        chartLabor.setData(lineData);
        
        XAxis xAxis = chartLabor.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        
        chartLabor.getDescription().setEnabled(false);
        chartLabor.invalidate(); // Refresh
    }

    private void updateTaskChart(List<String> statuses) {
        Map<String, Integer> statusCounts = new HashMap<>();
        for (String status : statuses) {
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        chartTasks.setData(pieData);
        chartTasks.getDescription().setEnabled(false);
        chartTasks.setCenterText("Tasks");
        chartTasks.invalidate(); // Refresh
    }
}
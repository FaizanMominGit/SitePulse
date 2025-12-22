package com.example.sitepulse;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.DailyReport;
import com.example.sitepulse.data.local.entity.Project;
import com.example.sitepulse.util.PdfGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DprDetailActivity extends AppCompatActivity {

    private TextView tvDetailTitle, tvDetailDate, tvDetailLaborCount, tvDetailWorkDescription, tvDetailHindrances;
    private ImageView ivDetailSitePhoto;
    private Button btnDownloadPdf;

    private AppDatabase db;
    private String reportId;
    private DailyReport currentReport;
    private Project currentProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dpr_detail);

        if (getIntent().hasExtra("REPORT_ID")) {
            reportId = getIntent().getStringExtra("REPORT_ID");
        } else {
            Toast.makeText(this, "Error: No Report ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getDatabase(this);

        initViews();
        loadData();
    }

    private void initViews() {
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailLaborCount = findViewById(R.id.tvDetailLaborCount);
        tvDetailWorkDescription = findViewById(R.id.tvDetailWorkDescription);
        tvDetailHindrances = findViewById(R.id.tvDetailHindrances);
        ivDetailSitePhoto = findViewById(R.id.ivDetailSitePhoto);
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf);

        btnDownloadPdf.setOnClickListener(v -> {
            if (currentReport != null && currentProject != null) {
                generateAndSavePdf();
            } else {
                Toast.makeText(this, "Report data not loaded yet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        db.dailyReportDao().getReportById(reportId).observe(this, report -> {
            if (report != null) {
                currentReport = report;
                db.projectDao().getProjectById(report.projectId).observe(this, project -> {
                    if (project != null) {
                        currentProject = project;
                        populateUi(report, project);
                    }
                });
            }
        });
    }

    private void populateUi(DailyReport report, Project project) {
        tvDetailTitle.setText(project.name + " - Report");
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        tvDetailDate.setText(sdf.format(new Date(report.date)));

        tvDetailLaborCount.setText(String.valueOf(report.laborCount));
        tvDetailWorkDescription.setText(report.workDescription);
        tvDetailHindrances.setText(report.hindrances != null ? report.hindrances : "None");

        String imageSource = report.isSynced && report.imageUrl != null ? report.imageUrl : report.imagePath;
        if (imageSource != null) {
            Glide.with(this)
                    .load(imageSource)
                    .into(ivDetailSitePhoto);
        }
    }

    private void generateAndSavePdf() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Generating PDF...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String fileName = "DPR_" + currentReport.id + ".pdf";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        PdfGenerator.generateDprPdf(this, currentProject, currentReport, outputStream, new PdfGenerator.PdfGenerationListener() {
                            @Override
                            public void onComplete() {
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    openPdf(uri);
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(DprDetailActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to create output stream", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) downloadsDir.mkdirs();
                File pdfFile = new File(downloadsDir, fileName);
                
                OutputStream outputStream = new FileOutputStream(pdfFile);
                PdfGenerator.generateDprPdf(this, currentProject, currentReport, outputStream, new PdfGenerator.PdfGenerationListener() {
                    @Override
                    public void onComplete() {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Uri fileUri = FileProvider.getUriForFile(DprDetailActivity.this, "com.example.sitepulse.fileprovider", pdfFile);
                            openPdf(fileUri);
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(DprDetailActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openPdf(Uri pdfUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No PDF viewer installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
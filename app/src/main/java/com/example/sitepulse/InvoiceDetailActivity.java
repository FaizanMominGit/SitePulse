package com.example.sitepulse;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.entity.Invoice;
import com.example.sitepulse.util.PdfGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoiceDetailActivity extends AppCompatActivity {

    private TextView tvInvoiceNumber, tvDate, tvClientName, tvDescription, tvSubtotal, tvGstAmount, tvTotalAmount, tvStatus;
    private Button btnSharePdf;
    private AppDatabase db;
    private String invoiceId;
    private Invoice currentInvoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);

        db = AppDatabase.getDatabase(this);

        if (getIntent().hasExtra("INVOICE_ID")) {
            invoiceId = getIntent().getStringExtra("INVOICE_ID");
        } else {
            Toast.makeText(this, "Invoice ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadInvoice();
    }

    private void initViews() {
        tvInvoiceNumber = findViewById(R.id.tvInvoiceNumber);
        tvDate = findViewById(R.id.tvDate);
        tvClientName = findViewById(R.id.tvClientName);
        tvDescription = findViewById(R.id.tvDescription);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvGstAmount = findViewById(R.id.tvGstAmount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvStatus = findViewById(R.id.tvStatus);
        btnSharePdf = findViewById(R.id.btnSharePdf);

        btnSharePdf.setOnClickListener(v -> {
            if (currentInvoice != null) {
                generateAndSharePdf();
            }
        });
    }

    private void loadInvoice() {
        db.invoiceDao().getInvoiceById(invoiceId).observe(this, invoice -> {
            if (invoice != null) {
                currentInvoice = invoice;
                bindData(invoice);
            }
        });
    }

    private void bindData(Invoice invoice) {
        tvInvoiceNumber.setText(invoice.invoiceNumber);
        tvDate.setText(formatDate(invoice.date));
        tvClientName.setText(invoice.clientName);
        tvDescription.setText(invoice.description);
        tvStatus.setText(invoice.status);

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        tvSubtotal.setText(format.format(invoice.subtotal));
        tvGstAmount.setText(format.format(invoice.gstAmount) + " (" + invoice.gstRate + "%)");
        tvTotalAmount.setText(format.format(invoice.totalAmount));
    }

    private String formatDate(long timestamp) {
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(timestamp));
    }

    private void generateAndSharePdf() {
        File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), 
                "Invoice_" + currentInvoice.invoiceNumber + ".pdf");

        try {
            OutputStream outputStream = new FileOutputStream(pdfFile);
            PdfGenerator.generateInvoicePdf(this, currentInvoice, outputStream, new PdfGenerator.PdfGenerationListener() {
                @Override
                public void onComplete() {
                    sharePdf(pdfFile);
                    updateInvoiceStatus();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(InvoiceDetailActivity.this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error creating file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePdf(File file) {
        Uri uri = FileProvider.getUriForFile(this, "com.example.sitepulse.fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share Invoice via"));
    }

    private void updateInvoiceStatus() {
        if (!"SENT".equals(currentInvoice.status) && !"PAID".equals(currentInvoice.status)) {
            currentInvoice.status = "SENT";
            currentInvoice.isSynced = false; // Mark for sync
            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.invoiceDao().update(currentInvoice);
            });
        }
    }
}
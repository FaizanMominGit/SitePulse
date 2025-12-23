package com.example.sitepulse.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.sitepulse.data.local.entity.DailyReport;
import com.example.sitepulse.data.local.entity.Invoice;
import com.example.sitepulse.data.local.entity.Project;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdfGenerator {

    public interface PdfGenerationListener {
        void onComplete();
        void onError(Exception e);
    }

    public static void generateDprPdf(Context context, Project project, DailyReport report, OutputStream outputStream, PdfGenerationListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                PdfWriter writer = new PdfWriter(outputStream);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc, PageSize.A4);
                document.setMargins(36, 36, 36, 36);

                // Title
                document.add(new Paragraph("Daily Progress Report")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setFontSize(20));

                // Project Info Table
                Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
                infoTable.addCell(createCell("Project Name:", true));
                infoTable.addCell(createCell(project.name, false));
                infoTable.addCell(createCell("Date:", true));
                infoTable.addCell(createCell(new SimpleDateFormat("MMMM d, yyyy", Locale.US).format(new Date(report.date)), false));
                document.add(infoTable);

                document.add(new Paragraph("\n"));

                // Report Details Table
                Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
                detailsTable.addCell(createCell("Labor Count:", true));
                detailsTable.addCell(createCell(String.valueOf(report.laborCount), false));
                detailsTable.addCell(createCell("Work Description:", true));
                detailsTable.addCell(createCell(report.workDescription, false));
                detailsTable.addCell(createCell("Hindrances:", true));
                detailsTable.addCell(createCell(report.hindrances != null ? report.hindrances : "None", false));
                document.add(detailsTable);
                
                // Image Loading must happen on the main thread with Glide
                handler.post(() -> {
                    String imageSource = report.isSynced && report.imageUrl != null ? report.imageUrl : report.imagePath;
                    if (imageSource != null) {
                        Glide.with(context)
                                .asBitmap()
                                .load(imageSource)
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        executor.execute(() -> {
                                            try {
                                                document.add(new Paragraph("\nSite Photo:").setBold());
                                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                                resource.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                                                ImageData imageData = ImageDataFactory.create(stream.toByteArray());
                                                Image pdfImage = new Image(imageData).setAutoScale(true);
                                                document.add(pdfImage);
                                                document.close();
                                                handler.post(listener::onComplete);
                                            } catch (Exception e) {
                                                handler.post(() -> listener.onError(e));
                                            }
                                        });
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                        // Handle case where image load is cleared, just close the doc
                                        executor.execute(() -> {
                                            document.close();
                                            handler.post(listener::onComplete);
                                        });
                                    }
                                });
                    } else {
                        // No image, just close the document
                        document.close();
                        handler.post(listener::onComplete);
                    }
                });

            } catch (Exception e) {
                handler.post(() -> listener.onError(e));
            }
        });
    }

    public static void generateInvoicePdf(Context context, Invoice invoice, OutputStream outputStream, PdfGenerationListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                PdfWriter writer = new PdfWriter(outputStream);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc, PageSize.A4);
                document.setMargins(36, 36, 36, 36);

                // Title
                document.add(new Paragraph("INVOICE")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setFontSize(24));

                document.add(new Paragraph("\n"));

                // Invoice Header Info
                Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
                headerTable.addCell(createCell("Invoice Number: " + invoice.invoiceNumber, true));
                headerTable.addCell(createCell("Date: " + new SimpleDateFormat("MMM d, yyyy", Locale.US).format(new Date(invoice.date)), true).setTextAlignment(TextAlignment.RIGHT));
                document.add(headerTable);

                document.add(new Paragraph("\n"));
                
                // Client Info
                document.add(new Paragraph("Bill To:").setBold());
                document.add(new Paragraph(invoice.clientName).setFontSize(14));
                
                document.add(new Paragraph("\n"));

                // Items Table (Currently just one item based on description)
                float[] columnWidths = {4, 1, 1}; // Description, Amount
                Table itemTable = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
                
                // Headers
                itemTable.addCell(createCell("Description", true));
                itemTable.addCell(createCell("Amount", true).setTextAlignment(TextAlignment.RIGHT));
                
                // Row
                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                itemTable.addCell(createCell(invoice.description, false));
                itemTable.addCell(createCell(format.format(invoice.subtotal), false).setTextAlignment(TextAlignment.RIGHT));
                
                document.add(itemTable);
                
                document.add(new Paragraph("\n"));

                // Totals Table
                Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
                
                totalsTable.addCell(createCell("Subtotal:", true).setTextAlignment(TextAlignment.RIGHT));
                totalsTable.addCell(createCell(format.format(invoice.subtotal), false).setTextAlignment(TextAlignment.RIGHT));
                
                totalsTable.addCell(createCell("GST (" + invoice.gstRate + "%):", true).setTextAlignment(TextAlignment.RIGHT));
                totalsTable.addCell(createCell(format.format(invoice.gstAmount), false).setTextAlignment(TextAlignment.RIGHT));
                
                totalsTable.addCell(createCell("Total Amount:", true).setTextAlignment(TextAlignment.RIGHT).setFontSize(14));
                totalsTable.addCell(createCell(format.format(invoice.totalAmount), true).setTextAlignment(TextAlignment.RIGHT).setFontSize(14));

                document.add(totalsTable);

                document.close();
                handler.post(listener::onComplete);

            } catch (Exception e) {
                handler.post(() -> listener.onError(e));
            }
        });
    }

    private static Cell createCell(String text, boolean isHeader) {
        Paragraph p = new Paragraph(text);
        Cell cell = new Cell().add(p);
        if (isHeader) {
            cell.setBold();
        }
        return cell;
    }
}
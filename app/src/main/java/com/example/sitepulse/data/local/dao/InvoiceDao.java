package com.example.sitepulse.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sitepulse.data.local.entity.Invoice;

import java.util.List;

@Dao
public interface InvoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Invoice invoice);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Invoice> invoices);

    @Update
    void update(Invoice invoice);

    @Query("SELECT * FROM invoices WHERE projectId = :projectId ORDER BY date DESC")
    LiveData<List<Invoice>> getInvoicesForProject(String projectId);
    
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    LiveData<List<Invoice>> getAllInvoices();

    @Query("SELECT * FROM invoices WHERE id = :invoiceId LIMIT 1")
    LiveData<Invoice> getInvoiceById(String invoiceId);

    @Query("SELECT * FROM invoices WHERE isSynced = 0")
    List<Invoice> getUnsyncedInvoices();
    
    // For Dashboard - Get total invoiced amount
    @Query("SELECT SUM(totalAmount) FROM invoices WHERE projectId = :projectId")
    LiveData<Double> getTotalInvoicedAmount(String projectId);

    @Query("DELETE FROM invoices")
    void clearAll();
}
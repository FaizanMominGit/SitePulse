package com.example.sitepulse.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sitepulse.data.local.entity.MaterialRequest;

import java.util.List;

@Dao
public interface MaterialRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MaterialRequest request);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MaterialRequest> requests);

    @Update
    void update(MaterialRequest request);

    @Query("SELECT * FROM material_requests WHERE projectId = :projectId ORDER BY date DESC")
    LiveData<List<MaterialRequest>> getRequestsForProject(String projectId);
    
    @Query("SELECT * FROM material_requests ORDER BY date DESC")
    LiveData<List<MaterialRequest>> getAllRequests();

    // For Dashboard - Estimated Material Cost
    @Query("SELECT SUM(estimatedCost) FROM material_requests WHERE projectId = :projectId")
    Double getTotalEstimatedCost(String projectId);

    @Query("SELECT * FROM material_requests WHERE isSynced = 0")
    List<MaterialRequest> getUnsyncedRequests();

    @Query("DELETE FROM material_requests")
    void clearAll();
}
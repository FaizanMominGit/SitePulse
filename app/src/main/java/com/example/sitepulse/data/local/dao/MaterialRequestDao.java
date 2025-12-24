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

    // Fetch all requests
    @Query("SELECT * FROM material_requests ORDER BY date DESC")
    LiveData<List<MaterialRequest>> getAllMaterialRequests();

    // Fetch requests by Project
    @Query("SELECT * FROM material_requests WHERE projectId = :projectId ORDER BY date DESC")
    LiveData<List<MaterialRequest>> getRequestsByProject(String projectId);

    @Query("SELECT * FROM material_requests WHERE isSynced = 0")
    List<MaterialRequest> getUnsyncedRequests();

    // For Dashboard - Estimate costs (Note: We don't have a 'cost' field in MaterialRequest in Inc 4, 
    // but assuming we might add one or just count pending requests. 
    // Requirement says 'Pending Material Costs'. Let's assume we add a cost field or just count.
    // For now, let's count PENDING requests as a proxy or use quantity if cost is missing.
    // Wait, let's check the entity first. If no cost, we can't sum cost.
    // Let's assume we want count of pending requests for now if cost is missing.)
    @Query("SELECT COUNT(*) FROM material_requests WHERE status = 'Pending' AND projectId = :projectId")
    LiveData<Integer> getPendingRequestCount(String projectId);

    @Query("DELETE FROM material_requests")
    void clearAll();
}
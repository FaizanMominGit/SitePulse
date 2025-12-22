package com.example.sitepulse.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sitepulse.data.local.entity.DailyReport;

import java.util.List;

@Dao
public interface DailyReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyReport report);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DailyReport> reports);

    @Update
    void update(DailyReport report);

    @Query("SELECT * FROM daily_reports WHERE projectId = :projectId ORDER BY date DESC")
    LiveData<List<DailyReport>> getReportsForProject(String projectId);

    @Query("SELECT * FROM daily_reports ORDER BY date DESC")
    LiveData<List<DailyReport>> getAllReports();

    @Query("SELECT * FROM daily_reports WHERE id = :reportId LIMIT 1")
    LiveData<DailyReport> getReportById(String reportId);

    @Query("SELECT * FROM daily_reports WHERE isSynced = 0")
    List<DailyReport> getUnsyncedReports();

    @Query("DELETE FROM daily_reports")
    void clearAll();
}
package com.example.sitepulse.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sitepulse.data.local.entity.Attendance;

import java.util.List;

@Dao
public interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Attendance attendance);

    @Update
    void update(Attendance attendance);

    // Get today's attendance for a specific user and project
    @Query("SELECT * FROM attendance WHERE userId = :userId AND projectId = :projectId AND clockInTime >= :startOfDay AND clockInTime < :endOfDay ORDER BY clockInTime DESC LIMIT 1")
    LiveData<Attendance> getTodayAttendanceForProject(String userId, String projectId, long startOfDay, long endOfDay);

    // Get today's attendance for a specific user (regardless of project)
    @Query("SELECT * FROM attendance WHERE userId = :userId AND clockInTime >= :startOfDay AND clockInTime < :endOfDay ORDER BY clockInTime DESC LIMIT 1")
    LiveData<Attendance> getTodayAttendance(String userId, long startOfDay, long endOfDay);

    @Query("SELECT * FROM attendance WHERE isSynced = 0")
    List<Attendance> getUnsyncedAttendance();

    @Query("DELETE FROM attendance")
    void clearAll();
}
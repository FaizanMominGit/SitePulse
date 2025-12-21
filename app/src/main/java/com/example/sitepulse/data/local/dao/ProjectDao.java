package com.example.sitepulse.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.sitepulse.data.local.entity.Project;
import java.util.List;

@Dao
public interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Project> projects);

    @Query("SELECT * FROM projects")
    LiveData<List<Project>> getAllProjects();

    // Added this method to fix the compilation error
    @Query("SELECT * FROM projects LIMIT 1")
    Project getFirstProject();

    @Query("DELETE FROM projects")
    void clearAll();
}
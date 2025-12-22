package com.example.sitepulse.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
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

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    LiveData<Project> getProjectById(String projectId);

    @Query("SELECT * FROM projects LIMIT 1")
    Project getFirstProject();

    // Find projects where assignedEngineerIds contains the userId
    // Note: This is a simple LIKE query. For robust CSV handling, normalized tables are better, 
    // but this works for our prototype with comma-separated IDs.
    @Query("SELECT * FROM projects WHERE assignedEngineerIds LIKE '%' || :userId || '%'")
    LiveData<List<Project>> getProjectsForEngineer(String userId);
    
    @Delete
    void delete(Project project);

    @Query("DELETE FROM projects")
    void clearAll();
}
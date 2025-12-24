package com.example.sitepulse.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sitepulse.data.local.entity.Project;
import java.util.List;

@Dao
public interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Project> projects);

    @Update
    void update(Project project);

    // Filter out archived projects by default
    @Query("SELECT * FROM projects WHERE isArchived = 0")
    LiveData<List<Project>> getAllProjects();

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    LiveData<Project> getProjectById(String projectId);

    @Query("SELECT * FROM projects WHERE isArchived = 0 LIMIT 1")
    Project getFirstProject();

    // Find projects where assignedEngineerIds contains the userId AND is not archived
    @Query("SELECT * FROM projects WHERE assignedEngineerIds LIKE '%' || :userId || '%' AND isArchived = 0")
    LiveData<List<Project>> getProjectsForEngineer(String userId);
    
    @Delete
    void delete(Project project);

    @Query("DELETE FROM projects")
    void clearAll();
}
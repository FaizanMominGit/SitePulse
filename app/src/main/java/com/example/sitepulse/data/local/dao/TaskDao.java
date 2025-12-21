package com.example.sitepulse.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.sitepulse.data.local.entity.Task;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Task> tasks);

    @Update
    void update(Task task);

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY timestamp DESC")
    LiveData<List<Task>> getTasksForProject(String projectId);
    
    @Query("SELECT * FROM tasks")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    List<Task> getUnsyncedTasks(); // Not LiveData, for Worker

    @Query("DELETE FROM tasks")
    void clearAll();
}
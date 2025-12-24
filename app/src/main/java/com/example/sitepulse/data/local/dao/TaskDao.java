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

    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    LiveData<List<Task>> getTasksForProject(String projectId);
    
    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    List<Task> getUnsyncedTasks();

    // For Dashboard - Task completion stats
    // Returns List of status strings. We will count manually in code or use GROUP BY if return type is complex
    // Simplified: Return completed count
    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId AND isCompleted = 1")
    LiveData<Integer> getCompletedTaskCount(String projectId);

    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId AND isCompleted = 0")
    LiveData<Integer> getPendingTaskCount(String projectId);

    @Query("DELETE FROM tasks")
    void clearAll();
}
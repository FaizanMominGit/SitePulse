package com.example.sitepulse.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.sitepulse.data.local.entity.User;
import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> users);

    @Query("SELECT * FROM users WHERE id = :uid LIMIT 1")
    LiveData<User> getUser(String uid);

    @Query("SELECT * FROM users WHERE role = 'Engineer'")
    LiveData<List<User>> getAllEngineers();

    @Query("DELETE FROM users")
    void clear();
}
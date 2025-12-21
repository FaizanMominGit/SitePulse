package com.example.sitepulse.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.sitepulse.data.local.dao.AttendanceDao;
import com.example.sitepulse.data.local.dao.DailyReportDao;
import com.example.sitepulse.data.local.dao.MaterialRequestDao;
import com.example.sitepulse.data.local.dao.ProjectDao;
import com.example.sitepulse.data.local.dao.TaskDao;
import com.example.sitepulse.data.local.dao.UserDao;
import com.example.sitepulse.data.local.entity.Attendance;
import com.example.sitepulse.data.local.entity.DailyReport;
import com.example.sitepulse.data.local.entity.MaterialRequest;
import com.example.sitepulse.data.local.entity.Project;
import com.example.sitepulse.data.local.entity.Task;
import com.example.sitepulse.data.local.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Updated version to 7 after adding MaterialRequest entity
@Database(entities = {Project.class, Task.class, User.class, Attendance.class, DailyReport.class, MaterialRequest.class}, version = 7, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ProjectDao projectDao();
    public abstract TaskDao taskDao();
    public abstract UserDao userDao();
    public abstract AttendanceDao attendanceDao();
    public abstract DailyReportDao dailyReportDao();
    public abstract MaterialRequestDao materialRequestDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "site_pulse_database")
                            .fallbackToDestructiveMigration() // Handle version upgrade by clearing DB for now
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
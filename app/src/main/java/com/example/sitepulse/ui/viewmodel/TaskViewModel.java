package com.example.sitepulse.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.dao.TaskDao;
import com.example.sitepulse.data.local.entity.Task;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskDao taskDao;
    private final LiveData<List<Task>> tasksForProject;

    public TaskViewModel(@NonNull Application application, String projectId) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        taskDao = db.taskDao();
        tasksForProject = taskDao.getTasksForProject(projectId);
    }

    public LiveData<List<Task>> getTasksForProject() {
        return tasksForProject;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final Application application;
        private final String projectId;

        public Factory(@NonNull Application application, String projectId) {
            this.application = application;
            this.projectId = projectId;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new TaskViewModel(application, projectId);
        }
    }
}
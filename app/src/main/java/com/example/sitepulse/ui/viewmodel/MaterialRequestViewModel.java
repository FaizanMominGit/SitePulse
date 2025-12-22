package com.example.sitepulse.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.sitepulse.data.local.AppDatabase;
import com.example.sitepulse.data.local.dao.MaterialRequestDao;
import com.example.sitepulse.data.local.entity.MaterialRequest;
import java.util.List;

public class MaterialRequestViewModel extends AndroidViewModel {

    private final MaterialRequestDao materialRequestDao;
    private final LiveData<List<MaterialRequest>> allMaterialRequests;

    public MaterialRequestViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        materialRequestDao = db.materialRequestDao();
        allMaterialRequests = materialRequestDao.getAllRequests();
    }

    public LiveData<List<MaterialRequest>> getAllMaterialRequests() {
        return allMaterialRequests;
    }
}
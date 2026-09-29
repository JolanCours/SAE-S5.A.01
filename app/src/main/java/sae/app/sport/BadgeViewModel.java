package com.example.myapplication;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel responsable de la préparation des données pour l'UI (Couche ViewModel).
 * Il survit aux changements de configuration (ex: rotation) et communique avec le Model.
 */
public class BadgeViewModel extends AndroidViewModel {
    private BadgeDao badgeDao;
    private LiveData<List<Badge>> allBadges;
    // Service d'exécution pour les tâches asynchrones en arrière-plan (écriture en DB)
    private ExecutorService executorService;

    public BadgeViewModel(@NonNull Application application) {
        super(application);
        // Initialisation de la base de données et récupération du DAO
        AppDatabase db = AppDatabase.getDatabase(application);
        badgeDao = db.badgeDao();
        // Récupération de la liste des badges sous forme de LiveData
        allBadges = badgeDao.getAllBadges();
        // Création d'un thread unique pour les opérations d'insertion
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Retourne la liste des badges observable par l'Activity ou le Fragment.
     */
    public LiveData<List<Badge>> getAllBadges() {
        return allBadges;
    }

    /**
     * Insère un badge de manière asynchrone pour ne pas bloquer le thread principal (UI).
     */
    public void insert(Badge badge) {
        executorService.execute(() -> badgeDao.insertBadge(badge));
    }
}

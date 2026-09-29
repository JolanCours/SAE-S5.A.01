package com.example.myapplication;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Interface DAO (Data Access Object) pour la table des badges.
 * Définit les requêtes SQL et les opérations de base de données (Couche Model).
 */
@Dao
public interface BadgeDao {
    
    /**
     * Récupère tous les badges triés par leur id.
     * Retourne un LiveData permettant à l'UI d'observer les changements en temps réel.
     */
    @Query("SELECT * FROM badges")
    LiveData<List<Badge>> getAllBadges();

    /**
     * Insère un nouveau badge dans la base de données.
     */
    @Insert
    void insertBadge(Badge badge);

    /**
     * Met à jour les informations d'un badge existant (ex: changement de l'état isUnlocked).
     */
    @Update
    void updateBadge(Badge badge);
}

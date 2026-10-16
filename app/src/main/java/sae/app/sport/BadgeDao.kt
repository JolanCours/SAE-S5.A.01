package sae.app.sport

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Interface DAO (Data Access Object) pour la table des badges (Kotlin/Room).
 * Définit les requêtes SQL et méthodes d'accès à la base de données.
 */
@Dao
interface BadgeDao {
    
    /**
     * Récupère tous les badges sous forme de LiveData pour une observation réactive.
     */
    @Query("SELECT * FROM badges")
    fun getAllBadges(): LiveData<List<Badge>>

    /**
     * Insère un nouveau badge dans la base de données.
     */
    @Insert
    fun insertBadge(badge: Badge)

    /**
     * Met à jour un badge existant dans la base de données.
     */
    @Update
    fun updateBadge(badge: Badge)
}

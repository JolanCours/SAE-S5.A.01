package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Interface DAO pour la table des badges (Kotlin).
 */
@Dao
interface BadgeDao {
    
    @Query("SELECT * FROM badges")
    fun getAllBadges(): LiveData<List<Badge>>

    @Insert
    fun insertBadge(badge: Badge)

    @Update
    fun updateBadge(badge: Badge)
}

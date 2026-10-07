package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Modèle de données représentant un Badge (Couche Model).
 */
@Entity(tableName = "badges")
data class Badge(
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val currentProgress: Int,
    val totalGoal: Int,
    val imageResId: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

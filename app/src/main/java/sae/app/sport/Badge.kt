package sae.app.sport

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Modèle de données représentant un Badge (Couche Model).
 * Correspond à une ligne dans la table SQLite "badges" via Room.
 *
 * @property title Le titre du badge.
 * @property description La description explicative du badge.
 * @property isUnlocked Indique si le badge a été débloqué ou non.
 * @property currentProgress La progression actuelle de l'utilisateur.
 * @property totalGoal L'objectif total requis pour débloquer le badge.
 * @property imageResId L'identifiant de la ressource image/icône du badge.
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

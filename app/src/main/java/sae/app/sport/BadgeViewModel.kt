package sae.app.sport

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * ViewModel pour les badges en Kotlin.
 * Gère la liste des badges en mémoire pour l'application sport.
 */
class BadgeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _allBadges = MutableLiveData<List<Badge>>().apply {
        value = listOf(
            Badge("Premier Pas", "Réussir son tout premier entraînement", true, 1, 1, R.drawable.monkey),
            Badge("Champion de la semaine", "Compléter 5 séances d'exercice", false, 3, 5, R.drawable.monkey)
        )
    }
    
    /** Liste observable de tous les badges. */
    val allBadges: LiveData<List<Badge>> = _allBadges

    /**
     * Insère un nouveau badge dans la liste en mémoire.
     */
    fun insert(badge: Badge) {
        val currentList = _allBadges.value.orEmpty().toMutableList()
        currentList.add(badge)
        _allBadges.value = currentList
    }
}

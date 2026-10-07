package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import java.util.concurrent.Executors

/**
 * ViewModel pour les badges en Kotlin.
 */
class BadgeViewModel(application: Application) : AndroidViewModel(application) {
    private val badgeDao: BadgeDao
    val allBadges: LiveData<List<Badge>>
    private val executorService = Executors.newSingleThreadExecutor()

    init {
        val db = AppDatabase.getDatabase(application)
        badgeDao = db.badgeDao()
        allBadges = badgeDao.getAllBadges()
    }

    fun insert(badge: Badge) {
        executorService.execute { badgeDao.insertBadge(badge) }
    }
}

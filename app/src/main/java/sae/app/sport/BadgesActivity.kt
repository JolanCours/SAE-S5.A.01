package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Activité principale gérant l'écran d'affichage des badges (Kotlin).
 */
class BadgesActivity : AppCompatActivity() {

    private lateinit var badgeViewModel: BadgeViewModel
    private lateinit var adapter: BadgeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badges)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewBadges)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        adapter = BadgeAdapter()
        recyclerView.adapter = adapter

        badgeViewModel = ViewModelProvider(this).get(BadgeViewModel::class.java)

        badgeViewModel.allBadges.observe(this) { badges ->
            if (badges != null && badges.isEmpty()) {
                // Insertion des badges par défaut si la liste est vide
                badgeViewModel.insert(Badge("Premier Pas", "Réussir son tout premier entraînement", true, 1, 1, R.drawable.monkey))
                badgeViewModel.insert(Badge("Champion de la semaine", "Compléter 5 séances d'exercice", false, 3, 5, R.drawable.monkey))
            } else if (badges != null) {
                adapter.setBadges(badges)
            }
        }
    }
}

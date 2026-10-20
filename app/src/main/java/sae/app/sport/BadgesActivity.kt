package sae.app.sport

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Activité principale gérant l'écran d'affichage des badges (Kotlin).
 * Initialise le RecyclerView, observe les données via le [BadgeViewModel]
 * et insère des données par défaut si la base de données est vide.
 */
class BadgesActivity : AppCompatActivity() {

    private lateinit var badgeViewModel: BadgeViewModel
    private lateinit var adapter: BadgeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badges)

        // Configuration du RecyclerView et de son gestionnaire de disposition
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewBadges)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        // Initialisation de l'adaptateur
        adapter = BadgeAdapter()
        recyclerView.adapter = adapter

        // Récupération du ViewModel associé à l'activité
        badgeViewModel = ViewModelProvider(this).get(BadgeViewModel::class.java)

        // Observation des changements dans la base de données
        badgeViewModel.allBadges.observe(this) { badges ->
            if (badges != null && badges.isEmpty()) {
                // Insertion des badges par défaut si la liste est vide (première utilisation)
                badgeViewModel.insert(Badge("Premier Pas", "Réussir son tout premier entraînement", true, 1, 1, R.drawable.monkey))
                badgeViewModel.insert(Badge("Champion de la semaine", "Compléter 5 séances d'exercice", false, 3, 5, R.drawable.monkey))
            } else if (badges != null) {
                adapter.setBadges(badges)
            }
        }
    }
}

package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Activité principale gérant l'écran d'affichage des badges (Couche View du MVVM).
 * Elle initialise l'interface utilisateur et observe les changements du ViewModel.
 */
public class BadgesActivity extends AppCompatActivity {

    private BadgeViewModel badgeViewModel;
    private BadgeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Associe le layout XML principal contenant le titre et le RecyclerView
        setContentView(R.layout.activity_badges);

        // Configuration du RecyclerView (Composant d'affichage de liste optimisé)
        RecyclerView recyclerView = findViewById(R.id.recyclerViewBadges);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Affichage en liste verticale standard
        recyclerView.setHasFixedSize(true); // Optimisation de performance si la taille du layout ne change pas

        // Initialisation et liaison de l'adaptateur personnalisé au RecyclerView
        adapter = new BadgeAdapter();
        recyclerView.setAdapter(adapter);

        // Initialisation du ViewModel lié au cycle de vie de cette Activité
        badgeViewModel = new ViewModelProvider(this).get(BadgeViewModel.class);

        // Observation réactive des changements de données dans la base Room
        badgeViewModel.getAllBadges().observe(this, badges -> {
            // Si la base de données est vide au tout premier lancement, on injecte des données par défaut avec progrès et icône
            if (badges != null && badges.isEmpty()) {
                badgeViewModel.insert(new Badge("Premier Pas", "Réussir son tout premier entraînement", true, 1, 1, R.drawable.monkey));
                badgeViewModel.insert(new Badge("Champion de la semaine", "Compléter 5 séances d'exercice", false, 3, 5, R.drawable.monkey));
            } else {
                // Sinon, on transmet la liste de badges mise à jour à l'adaptateur pour rafraîchir l'affichage
                adapter.setBadges(badges);
            }
        });
    }
}

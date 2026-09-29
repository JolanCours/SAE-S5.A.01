package sae.app.sport.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Les données affichées sur l'accueil (valeurs par défaut = 0)
data class HomeUiState(
    val steps: Int = 0,          // pas du jour
    val exercisesDone: Int = 0,  // exercices terminés
    val avgScore: Int = 0,       // score moyen en %
    val streakDays: Int = 0      // jours d'affilée
)

// L'écran d'accueil : il reçoit les données et une action pour le bouton
@Composable
fun HomeScreen(
    state: HomeUiState,
    onStartExercise: () -> Unit
) {
    // Column = éléments empilés de haut en bas
    Column(
        modifier = Modifier
            .fillMaxSize()   // prend tout l'écran
            .padding(16.dp), // marge autour
        verticalArrangement = Arrangement.spacedBy(16.dp) // espace entre éléments
    ) {
        Text("Accueil", style = MaterialTheme.typography.headlineMedium)

        StatCard("Pas aujourd'hui", state.steps.toString())
        StatCard("Exercices réalisés", state.exercisesDone.toString())

        // Row = éléments côte à côte
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // weight(1f) = chaque carte prend la même largeur
            StatCard("Score moyen", "${state.avgScore}%", Modifier.weight(1f))
            StatCard("Série", "${state.streakDays} j", Modifier.weight(1f))
        }

        // Prend l'espace libre pour pousser le bouton en bas
        Spacer(Modifier.weight(1f))

        Button(
            onClick = onStartExercise, // action donnée par l'appelant
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Commencer un exercice")
        }
    }
}

// Petite carte réutilisable : un titre + une valeur
@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineLarge)
        }
    }
}

// Aperçu dans Android Studio (onglet Split/Design), sans lancer l'app
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        state = HomeUiState(steps = 4200, exercisesDone = 3, avgScore = 82, streakDays = 2),
        onStartExercise = {}
    )
}
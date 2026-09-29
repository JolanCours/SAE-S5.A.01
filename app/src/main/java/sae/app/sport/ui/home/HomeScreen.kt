package sae.app.sport.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class HomeUiState(
    val steps: Int = 0,
    val exercisesDone: Int = 0,
    val avgScore: Int = 0,
    val streakDays: Int = 0
)

@Composable
fun HomeScreen(
    //Action des boutons
    state: HomeUiState,
    onStartExercise: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Positionnement du bouton paramètres à gauche + titre
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Paramètres" // lu par l'accessibilité
                )
            }
            Text("Accueil", style = MaterialTheme.typography.headlineMedium)
        }

        StatCard("Pas aujourd'hui", state.steps.toString())
        StatCard("Exercices réalisés", state.exercisesDone.toString())

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("Score moyen", "${state.avgScore}%", Modifier.weight(1f))
            StatCard("Série", "${state.streakDays} j", Modifier.weight(1f))
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onStartExercise,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Commencer un exercice")
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        state = HomeUiState(steps = 4200, exercisesDone = 3, avgScore = 82, streakDays = 2),
        onStartExercise = {},
        onOpenSettings = {}
    )
}
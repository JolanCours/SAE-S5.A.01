package sae.app.sport.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    gamificationEnabled: Boolean,                 // état actuel du on/off
    onGamificationChange: (Boolean) -> Unit,      // appelé quand on bascule
    onBack: () -> Unit                            // retour à l'accueil
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ligne du haut : bouton retour + titre
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("Retour") }
            Text("Paramètres", style = MaterialTheme.typography.headlineMedium)
        }

        // Une ligne de paramètre : texte à gauche, interrupteur à droite
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Activer les éléments de jeux", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = gamificationEnabled,
                onCheckedChange = onGamificationChange
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(gamificationEnabled = true, onGamificationChange = {}, onBack = {})
}
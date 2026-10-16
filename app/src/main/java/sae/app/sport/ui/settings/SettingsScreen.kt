package sae.app.sport.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sae.app.sport.gamification.GamificationType

@Composable
fun SettingsScreen(
    gamificationEnabled: Boolean,
    gamificationType: GamificationType,
    onGamificationChange: (Boolean) -> Unit,
    onTypeChange: (GamificationType) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("Retour") }
            Text("Paramètres", style = MaterialTheme.typography.headlineMedium)
        }

        // Encart "Éléments de jeu"
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ligne 1 : titre + interrupteur d'activation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Éléments de jeu", style = MaterialTheme.typography.titleMedium)
                    Switch(checked = gamificationEnabled, onCheckedChange = onGamificationChange)
                }

                Text("Type de gamification", style = MaterialTheme.typography.labelLarge)

                // Une ligne par type : toute la ligne est cliquable
                GamificationType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (gamificationEnabled) 1f else 0.38f) // aspect grisé
                            .selectable(
                                selected = gamificationType == type,
                                enabled = gamificationEnabled,             // bloque le clic
                                role = Role.RadioButton,
                                onClick = { onTypeChange(type) }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // onClick = null : c'est la ligne entière qui gère le clic
                        RadioButton(
                            selected = gamificationType == type,
                            onClick = null,
                            enabled = gamificationEnabled
                        )
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(type.label, style = MaterialTheme.typography.bodyLarge)
                            Text(type.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        gamificationEnabled = false,   // mets true pour voir l'état actif
        gamificationType = GamificationType.BADGES,
        onGamificationChange = {},
        onTypeChange = {},
        onBack = {}
    )
}

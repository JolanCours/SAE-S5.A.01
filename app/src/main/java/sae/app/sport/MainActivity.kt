package sae.app.sport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import sae.app.sport.data.SettingsRepository
import sae.app.sport.ui.schedule.ScheduleScreen
import sae.app.sport.ui.settings.SettingsScreen
import sae.app.sport.ui.theme.SAESportTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sauvegarde locale des paramètres (SharedPreferences)
        val settings = SettingsRepository(applicationContext)

        setContent {
            SAESportTheme {
                // false = planning, true = paramètres
                var showSettings by rememberSaveable { mutableStateOf(false) }

                // Valeurs relues depuis la sauvegarde au démarrage
                var gamificationEnabled by remember { mutableStateOf(settings.gamificationEnabled) }
                var gamificationType by remember { mutableStateOf(settings.gamificationType) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (showSettings) {
                            // Le bouton retour du téléphone ferme les paramètres
                            BackHandler { showSettings = false }

                            SettingsScreen(
                                gamificationEnabled = gamificationEnabled,
                                gamificationType = gamificationType,
                                onGamificationChange = { newValue ->
                                    gamificationEnabled = newValue          // met à jour l'écran
                                    settings.gamificationEnabled = newValue // sauvegarde
                                },
                                onTypeChange = { newType ->
                                    gamificationType = newType              // met à jour l'écran
                                    settings.gamificationType = newType     // sauvegarde
                                },
                                onBack = { showSettings = false }
                            )
                        } else {
                            Column {
                                // Ligne du haut : engrenage à gauche
                                Row {
                                    IconButton(onClick = { showSettings = true }) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Paramètres"
                                        )
                                    }
                                }
                                // Ton écran existant, inchangé
                                ScheduleScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

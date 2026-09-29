package sae.app.sport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import sae.app.sport.data.SettingsRepository
import sae.app.sport.ui.home.HomeScreen
import sae.app.sport.ui.home.HomeUiState
import sae.app.sport.ui.settings.SettingsScreen
import sae.app.sport.ui.theme.SAESportTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Accès à la sauvegarde des paramètres
        val settings = SettingsRepository(applicationContext)

        setContent {
            SAESportTheme {
                // Quel écran est affiché (survit à la rotation de l'écran)
                var showSettings by rememberSaveable { mutableStateOf(false) }
                // Valeur du paramètre, lue depuis la sauvegarde au départ
                var gamificationEnabled by remember { mutableStateOf(settings.gamificationEnabled) }

                //évite que le contenu passe sous la barre d'état
                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    if (showSettings) {
                        // Le bouton retour du téléphone ferme les paramètres
                        BackHandler { showSettings = false }

                        SettingsScreen(
                            gamificationEnabled = gamificationEnabled,
                            onGamificationChange = { newValue ->
                                gamificationEnabled = newValue        // met à jour l'écran
                                settings.gamificationEnabled = newValue // sauvegarde
                            },
                            onBack = { showSettings = false }
                        )
                    } else {
                        HomeScreen(
                            state = HomeUiState(steps = 4200, exercisesDone = 3, avgScore = 82, streakDays = 2),
                            onStartExercise = { },
                            onOpenSettings = { showSettings = true }
                        )
                    }
                }
            }
        }
    }
}
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import sae.app.sport.data.SettingsRepository
import sae.app.sport.gamification.GamificationType
import sae.app.sport.ui.home.HomeScreen
import sae.app.sport.ui.home.HomeUiState
import sae.app.sport.ui.settings.SettingsScreen
import sae.app.sport.ui.theme.SAESportTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Accès à la sauvegarde locale des paramètres
        val settings = SettingsRepository(applicationContext)

        setContent {
            SAESportTheme {
                // Écran affiché : false = accueil, true = paramètres
                var showSettings by rememberSaveable { mutableStateOf(false) }

                // Valeurs lues depuis la sauvegarde au démarrage
                var gamificationEnabled by remember { mutableStateOf(settings.gamificationEnabled) }
                var gamificationType by remember { mutableStateOf(settings.gamificationType) }

                // safeDrawingPadding = le contenu ne passe pas sous la barre d'état
                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
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
                        HomeScreen(
                            // Données de test en dur, à remplacer plus tard
                            state = HomeUiState(
                                steps = 4200,
                                exercisesDone = 3,
                                avgScore = 82,
                                streakDays = 2
                            ),
                            onStartExercise = { },
                            onOpenSettings = { showSettings = true }
                        )
                    }
                }
            }
        }
    }
}

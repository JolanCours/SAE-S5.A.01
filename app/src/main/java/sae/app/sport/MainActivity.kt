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
import sae.app.sport.Objectif.ObjectifListe
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

                // Permet de savoir si la page Paramètres est affichée
                var showSettings by rememberSaveable {
                    mutableStateOf(false)
                }

                // Permet de savoir si la page Objectifs est affichée
                var showObjectives by rememberSaveable {
                    mutableStateOf(false)
                }

                // Valeur du paramètre de gamification
                var gamificationEnabled by remember {
                    mutableStateOf(settings.gamificationEnabled)
                }

                // Évite que le contenu passe sous la barre d'état
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {

                    if (showSettings) {

                        // Le bouton retour du téléphone ferme les paramètres
                        BackHandler {
                            showSettings = false
                        }

                        SettingsScreen(
                            gamificationEnabled = gamificationEnabled,

                            onGamificationChange = { newValue ->
                                gamificationEnabled = newValue
                                settings.gamificationEnabled = newValue
                            },

                            onBack = {
                                showSettings = false
                            }
                        )

                    } else if (showObjectives) {

                        // Le bouton retour du téléphone revient à l'accueil
                        BackHandler {
                            showObjectives = false
                        }

                        ObjectifListe(
                            onBack = {
                                showObjectives = false
                            }
                        )

                    } else {

                        // Page principale
                        HomeScreen(
                            state = HomeUiState(
                                steps = 4200,
                                exercisesDone = 3,
                                avgScore = 82,
                                streakDays = 2
                            ),

                            onStartExercise = {
                                // Géré par un autre membre de l'équipe
                            },

                            onOpenSettings = {
                                showSettings = true
                            },

                            onOpenObjectives = {
                                showObjectives = true
                            }
                        )
                    }
                }
            }
        }
    }
}
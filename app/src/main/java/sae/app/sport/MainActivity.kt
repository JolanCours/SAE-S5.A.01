package sae.app.sport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import sae.app.sport.ui.home.HomeScreen
import sae.app.sport.ui.home.HomeUiState
import sae.app.sport.ui.theme.SAESportTheme
class MainActivity : ComponentActivity() {
    // Appelée au lancement de l'app
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tout ce qui est dans setContent devient l'écran
        setContent {
            // Le thème applique tes couleurs et polices (Color.kt, Type.kt)
            SAESportTheme {
                HomeScreen(
                    // Données de test en dur, à remplacer plus tard par les vraies
                    state = HomeUiState(
                        steps = 4200,
                        exercisesDone = 3,
                        avgScore = 82,
                        streakDays = 2
                    ),
                    // Vide pour l'instant : plus tard, ouvrira l'écran caméra
                    onStartExercise = { }
                )
            }
        }
    }
}
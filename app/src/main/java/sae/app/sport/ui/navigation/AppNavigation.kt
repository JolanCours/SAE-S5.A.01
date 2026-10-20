package sae.app.sport.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import sae.app.sport.data.MovementRepository
import sae.app.sport.ui.creation.CreationFlowScreen
import sae.app.sport.ui.home.HomeScreen
import sae.app.sport.ui.training.TrainingScreen

enum class AppScreen {
    HOME, CREATE, TRAIN
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val repository = remember { MovementRepository(context) }
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    when (currentScreen) {
        AppScreen.HOME -> HomeScreen(
            onCreateMovement = { currentScreen = AppScreen.CREATE },
            onTrainMovement = { currentScreen = AppScreen.TRAIN }
        )
        AppScreen.CREATE -> CreationFlowScreen(
            repository = repository,
            onFinished = { currentScreen = AppScreen.HOME }
        )
        AppScreen.TRAIN -> TrainingScreen(
            repository = repository,
            onBack = { currentScreen = AppScreen.HOME }
        )
    }
}

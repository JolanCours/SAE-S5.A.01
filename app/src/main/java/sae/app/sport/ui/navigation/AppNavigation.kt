package sae.app.sport.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import sae.app.sport.data.MovementRepository
import sae.app.sport.model.PoseExercise
import sae.app.sport.training.WorkoutEngine
import sae.app.sport.ui.creation.EditorScreen
import sae.app.sport.ui.home.ExerciseDetailScreen
import sae.app.sport.ui.home.HomeScreen
import sae.app.sport.ui.training.ReportScreen
import sae.app.sport.ui.training.TrainingSessionScreen

sealed interface Route {
    object Home : Route
    data class Detail(val exerciseId: String) : Route
    data class Train(val exerciseId: String, val attempt: Int = 0) : Route
    data class Report(val exerciseId: String, val report: WorkoutEngine.Report) : Route
    data class Editor(val exerciseId: String?) : Route
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val repository = remember { MovementRepository(context.applicationContext) }
    val stack = remember { mutableStateListOf<Route>(Route.Home) }
    var dataVersion by remember { mutableStateOf(0) }
    var forward by remember { mutableStateOf(true) }

    val exercises = remember(dataVersion) { repository.getAllExercises() }
    fun find(id: String): PoseExercise? = exercises.firstOrNull { it.id == id }

    fun push(r: Route) { forward = true; stack.add(r) }
    fun pop() { forward = false; if (stack.size > 1) stack.removeAt(stack.lastIndex) }
    fun replaceTop(r: Route) { forward = true; stack[stack.lastIndex] = r }

    BackHandler(enabled = stack.size > 1 && stack.last() is Route.Detail) { pop() }

    AnimatedContent(
        targetState = stack.last(),
        transitionSpec = {
            if (forward) {
                (slideInHorizontally { it / 4 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 4 } + fadeOut())
            } else {
                (slideInHorizontally { -it / 4 } + fadeIn()) togetherWith (slideOutHorizontally { it / 4 } + fadeOut())
            }
        },
        label = "nav"
    ) { route ->
        when (route) {
            Route.Home -> HomeScreen(
                builtIns = exercises.filter { it.builtIn },
                custom = exercises.filter { !it.builtIn },
                onOpen = { push(Route.Detail(it.id)) },
                onCreate = { push(Route.Editor(null)) }
            )

            is Route.Detail -> {
                val ex = find(route.exerciseId)
                if (ex == null) {
                    LaunchedEffect(route) { if (stack.lastOrNull() == route) pop() }
                } else {
                    ExerciseDetailScreen(
                        exercise = ex,
                        onBack = { pop() },
                        onStart = { push(Route.Train(ex.id)) },
                        onEdit = { push(Route.Editor(ex.id)) },
                        onDelete = {
                            repository.deleteExercise(ex.id)
                            dataVersion++
                            pop()
                        }
                    )
                }
            }

            is Route.Train -> {
                val ex = find(route.exerciseId)
                if (ex == null) {
                    LaunchedEffect(route) { if (stack.lastOrNull() == route) pop() }
                } else {
                    TrainingSessionScreen(
                        exercise = ex,
                        onQuit = { pop() },
                        onFinished = { report -> replaceTop(Route.Report(ex.id, report)) }
                    )
                }
            }

            is Route.Report -> {
                val ex = find(route.exerciseId)
                if (ex == null) {
                    LaunchedEffect(route) { if (stack.lastOrNull() == route) pop() }
                } else {
                    ReportScreen(
                        exercise = ex,
                        report = route.report,
                        onContinue = { pop() },
                        onRetry = { replaceTop(Route.Train(ex.id, attempt = System.currentTimeMillis().toInt())) }
                    )
                }
            }

            is Route.Editor -> {
                val existing = route.exerciseId?.let { find(it) }
                val initial = remember(route) { existing ?: PoseExercise(name = "", steps = emptyList(), colorIndex = exercises.size) }
                EditorScreen(
                    initial = initial,
                    fromBuiltIn = existing?.builtIn == true,
                    onSave = { saved ->
                        repository.saveExercise(saved)
                        dataVersion++
                        pop()
                        if (stack.last() is Route.Detail) pop()
                        push(Route.Detail(saved.id))
                    },
                    onCancel = { pop() }
                )
            }
        }
    }
}

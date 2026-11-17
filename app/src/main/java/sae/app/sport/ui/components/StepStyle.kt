package sae.app.sport.ui.components

import androidx.compose.ui.graphics.Color
import sae.app.sport.model.ExerciseStep
import sae.app.sport.model.StepType
import sae.app.sport.ui.theme.Blue
import sae.app.sport.ui.theme.BlueDark
import sae.app.sport.ui.theme.Orange
import sae.app.sport.ui.theme.OrangeDark
import sae.app.sport.ui.theme.Purple
import sae.app.sport.ui.theme.PurpleDark

fun StepType.color(): Color = when (this) {
    StepType.HOLD -> Blue
    StepType.INSTANT -> Orange
    StepType.PAUSE -> Purple
}

fun StepType.shadow(): Color = when (this) {
    StepType.HOLD -> BlueDark
    StepType.INSTANT -> OrangeDark
    StepType.PAUSE -> PurpleDark
}

fun StepType.label(): String = when (this) {
    StepType.HOLD -> "Tenir"
    StepType.INSTANT -> "Instant"
    StepType.PAUSE -> "Pause"
}

fun StepType.emoji(): String = when (this) {
    StepType.HOLD -> "⏱️"
    StepType.INSTANT -> "⚡"
    StepType.PAUSE -> "☕"
}

fun StepType.instruction(): String = when (this) {
    StepType.HOLD -> "Tiens la pose"
    StepType.INSTANT -> "Prends la pose"
    StepType.PAUSE -> "Pause"
}

fun formatSec(sec: Float): String =
    if (sec % 1f == 0f) "${sec.toInt()} s" else "%.1f s".format(sec).replace('.', ',')

fun ExerciseStep.summary(): String = when (type) {
    StepType.HOLD -> "Tenir ${formatSec(safeDuration)}" + if (safeReps > 1) " × $safeReps" else ""
    StepType.INSTANT -> "Instant" + if (safeReps > 1) " × $safeReps" else ""
    StepType.PAUSE -> "Pause ${formatSec(safeDuration)}"
}

private val PRAISES = listOf("Bravo !", "Super !", "Parfait !", "Excellent !", "Bien joué !", "Trop fort !", "Nickel !")

fun praise(seed: Int): String = PRAISES[((seed % PRAISES.size) + PRAISES.size) % PRAISES.size]

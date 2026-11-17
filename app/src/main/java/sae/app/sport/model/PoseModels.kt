package sae.app.sport.model

import java.util.UUID

data class LandmarkPoint(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val visibility: Float? = null
)

data class RecordedPose(
    val landmarks: List<LandmarkPoint> = emptyList(),
    val aspect: Float = 0.75f
)

data class MovementSequence(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val poses: List<RecordedPose> = emptyList()
)

enum class StepType {
    HOLD,
    INSTANT,
    PAUSE
}

enum class Difficulty(val label: String, val threshold: Float) {
    EASY("Facile", 0.64f),
    NORMAL("Normal", 0.72f),
    HARD("Difficile", 0.80f)
}

data class ExerciseStep(
    val id: String = UUID.randomUUID().toString(),
    val type: StepType = StepType.HOLD,
    val pose: RecordedPose? = null,
    val durationSec: Float = 3f,
    val reps: Int = 1,
    val name: String = ""
) {
    val safeReps: Int get() = reps.coerceIn(1, 99)
    val safeDuration: Float get() = durationSec.coerceIn(0.5f, 600f)
    val isPose: Boolean get() = type != StepType.PAUSE

    fun displayName(index: Int): String = name.ifBlank {
        when (type) {
            StepType.PAUSE -> "Pause"
            else -> "Pose ${index + 1}"
        }
    }

    fun estimatedSec(): Float = when (type) {
        StepType.HOLD -> (safeDuration + 1.5f) * safeReps
        StepType.INSTANT -> 2f * safeReps
        StepType.PAUSE -> safeDuration
    }
}

data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val emoji: String = "💪",
    val colorIndex: Int = 0,
    val steps: List<ExerciseStep> = emptyList(),
    val rounds: Int = 1,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val builtIn: Boolean = false,
    val updatedAt: Long = 0L
) {
    val safeRounds: Int get() = rounds.coerceIn(1, 50)
    val safeDifficulty: Difficulty get() = difficulty ?: Difficulty.NORMAL

    val poseStepCount: Int get() = steps.count { it.isPose }

    fun estimatedDurationSec(): Int =
        (steps.sumOf { it.estimatedSec().toDouble() } * safeRounds).toInt()

    fun sanitized(): Exercise = copy(
        name = name ?: "",
        description = description ?: "",
        emoji = emoji ?: "💪",
        steps = (steps ?: emptyList()).map { s ->
            s.copy(
                id = s.id ?: UUID.randomUUID().toString(),
                type = s.type ?: StepType.HOLD,
                name = s.name ?: "",
                reps = s.safeReps,
                durationSec = s.safeDuration,
                pose = s.pose?.let { p ->
                    RecordedPose(
                        landmarks = p.landmarks ?: emptyList(),
                        aspect = if (p.aspect > 0f) p.aspect else 0.75f
                    )
                }
            )
        },
        rounds = safeRounds,
        difficulty = safeDifficulty
    )
}

fun formatDuration(totalSec: Int): String {
    val m = totalSec / 60
    val s = totalSec % 60
    return if (m > 0) "${m} min ${"%02d".format(s)}" else "${s} s"
}

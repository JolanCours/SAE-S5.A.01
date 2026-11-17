package sae.app.sport.training

import sae.app.sport.model.Exercise
import sae.app.sport.model.ExerciseStep
import sae.app.sport.model.StepType
import kotlin.math.exp
import kotlin.math.max

class WorkoutEngine(
    val exercise: Exercise,
    private val getReadyMs: Long = 3000L,
    private val threshold: Float = exercise.safeDifficulty.threshold
) {
    data class PlanItem(val step: ExerciseStep, val stepIndex: Int, val round: Int)

    enum class Phase { GET_READY, ACTIVE, REP_BREAK, SUCCESS, FINISHED }

    data class StepResult(
        val stepIndex: Int,
        val round: Int,
        val name: String,
        val type: StepType,
        val success: Boolean,
        val skipped: Boolean,
        val activeMs: Long,
        val quality: Float,
        val repsDone: Int,
        val repsTarget: Int
    )

    data class Report(
        val exerciseName: String,
        val results: List<StepResult>,
        val totalMs: Long,
        val accuracy: Float,
        val maxCombo: Int,
        val stars: Int,
        val successCount: Int,
        val poseCount: Int,
        val totalReps: Int
    )

    data class State(
        val phase: Phase,
        val index: Int,
        val item: PlanItem?,
        val phaseElapsedMs: Long,
        val remainingMs: Long,
        val holdProgress: Float,
        val repsDone: Int,
        val repsTarget: Int,
        val matched: Boolean,
        val score: Float,
        val combo: Int,
        val progress: Float,
        val totalElapsedMs: Long,
        val repEvents: Int,
        val successEvents: Int
    )

    val plan: List<PlanItem> = buildList {
        val steps = exercise.steps.filter { it.type == StepType.PAUSE || it.pose != null }
        for (r in 0 until exercise.safeRounds) {
            steps.forEachIndexed { i, s ->
                add(PlanItem(s, exercise.steps.indexOf(s).takeIf { it >= 0 } ?: i, r))
            }
        }
        while (isNotEmpty() && last().step.type == StepType.PAUSE) removeAt(lastIndex)
    }

    private var phase = if (plan.isEmpty()) Phase.FINISHED else Phase.GET_READY
    private var index = 0
    private var phaseElapsed = 0L
    private var totalElapsed = 0L

    private var smoothed = 0f
    private var resetSmoothing = true
    private var matched = false

    private var holdMs = 0L
    private var repsDone = 0
    private var armed = true
    private var confirmMs = 0L
    private var peak = 0f

    private var activeMs = 0L
    private var matchedScoreSum = 0.0
    private var matchedMs = 0L
    private val repQualities = mutableListOf<Float>()

    private var combo = 0
    private var maxCombo = 0
    private var repEvents = 0
    private var successEvents = 0

    private val results = mutableListOf<StepResult>()

    val currentItem: PlanItem? get() = plan.getOrNull(index)
    val currentStep: ExerciseStep? get() = currentItem?.step
    val isFinished: Boolean get() = phase == Phase.FINISHED

    companion object {
        const val HYSTERESIS = 0.05f
        const val EXIT_MARGIN = 0.10f
        const val CONFIRM_MS = 180L
        const val SUCCESS_MS = 700L
        const val REP_BREAK_MS = 1500L
        const val SMOOTHING_MS = 120f
    }

    fun tick(dtMs: Long, rawScore: Float?) {
        if (phase == Phase.FINISHED) return
        val dt = dtMs.coerceIn(0L, 250L)
        totalElapsed += dt
        phaseElapsed += dt

        val raw = rawScore ?: 0f
        if (resetSmoothing) {
            smoothed = raw
            resetSmoothing = false
        } else {
            val alpha = 1f - exp(-dt / SMOOTHING_MS)
            smoothed += alpha * (raw - smoothed)
        }

        when (phase) {
            Phase.GET_READY -> if (phaseElapsed >= getReadyMs) startItem(0)
            Phase.REP_BREAK -> if (phaseElapsed >= REP_BREAK_MS) {
                phase = Phase.ACTIVE
                phaseElapsed = 0
            }
            Phase.SUCCESS -> if (phaseElapsed >= SUCCESS_MS) startItem(index + 1)
            Phase.ACTIVE -> tickActive(dt)
            Phase.FINISHED -> Unit
        }
    }

    private fun tickActive(dt: Long) {
        val step = currentStep ?: return
        when (step.type) {
            StepType.PAUSE -> {
                if (phaseElapsed >= (step.safeDuration * 1000).toLong()) completeItem(success = true, skipped = false)
            }

            StepType.HOLD -> {
                activeMs += dt
                matched = smoothed >= threshold || (matched && smoothed >= threshold - HYSTERESIS)
                if (matched) {
                    holdMs += dt
                    matchedMs += dt
                    matchedScoreSum += smoothed * dt
                }
                if (holdMs >= (step.safeDuration * 1000).toLong()) {
                    repsDone++
                    repEvents++
                    repQualities += if (matchedMs > 0) (matchedScoreSum / matchedMs).toFloat() else smoothed
                    matchedMs = 0; matchedScoreSum = 0.0
                    if (repsDone >= step.safeReps) {
                        completeItem(success = true, skipped = false)
                    } else {
                        holdMs = 0
                        matched = false
                        phase = Phase.REP_BREAK
                        phaseElapsed = 0
                    }
                }
            }

            StepType.INSTANT -> {
                activeMs += dt
                if (armed) {
                    if (smoothed >= threshold) {
                        confirmMs += dt
                        peak = max(peak, smoothed)
                        if (confirmMs >= CONFIRM_MS) {
                            repsDone++
                            repEvents++
                            repQualities += peak
                            armed = false
                            confirmMs = 0
                            peak = 0f
                            if (repsDone >= step.safeReps) {
                                matched = true
                                completeItem(success = true, skipped = false)
                                return
                            }
                        }
                    } else {
                        confirmMs = 0
                    }
                    matched = confirmMs > 0
                } else {
                    matched = true
                    if (smoothed < threshold - EXIT_MARGIN) {
                        armed = true
                        matched = false
                    }
                }
            }
        }
    }

    private fun startItem(newIndex: Int) {
        val previous = if (phase == Phase.GET_READY) null else currentItem
        index = newIndex
        val item = currentItem
        if (item == null) {
            phase = Phase.FINISHED
            return
        }
        phase = Phase.ACTIVE
        phaseElapsed = 0
        holdMs = 0
        repsDone = 0
        confirmMs = 0
        peak = 0f
        activeMs = 0
        matchedMs = 0
        matchedScoreSum = 0.0
        repQualities.clear()
        matched = false
        resetSmoothing = true
        armed = !(item.step.type == StepType.INSTANT && previous != null && previous.step.id == item.step.id)
    }

    private fun completeItem(success: Boolean, skipped: Boolean) {
        val item = currentItem ?: return
        val step = item.step
        if (step.type != StepType.PAUSE) {
            results += StepResult(
                stepIndex = item.stepIndex,
                round = item.round,
                name = step.displayName(item.stepIndex),
                type = step.type,
                success = success,
                skipped = skipped,
                activeMs = activeMs,
                quality = if (success && repQualities.isNotEmpty()) repQualities.average().toFloat() else 0f,
                repsDone = repsDone,
                repsTarget = step.safeReps
            )
            if (success) {
                combo++
                maxCombo = max(maxCombo, combo)
                successEvents++
            } else {
                combo = 0
            }
        }
        if (success && step.type != StepType.PAUSE) {
            phase = Phase.SUCCESS
            phaseElapsed = 0
        } else {
            startItem(index + 1)
        }
    }

    fun skip() {
        when (phase) {
            Phase.GET_READY -> startItem(0)
            Phase.ACTIVE, Phase.REP_BREAK -> {
                val isPause = currentStep?.type == StepType.PAUSE
                completeItem(success = isPause, skipped = !isPause)
            }
            Phase.SUCCESS -> startItem(index + 1)
            Phase.FINISHED -> Unit
        }
    }

    fun state(): State {
        val item = currentItem
        val step = item?.step
        val remaining = when (phase) {
            Phase.GET_READY -> getReadyMs - phaseElapsed
            Phase.REP_BREAK -> REP_BREAK_MS - phaseElapsed
            Phase.ACTIVE -> when (step?.type) {
                StepType.PAUSE -> (step.safeDuration * 1000).toLong() - phaseElapsed
                StepType.HOLD -> (step.safeDuration * 1000).toLong() - holdMs
                else -> 0L
            }
            else -> 0L
        }.coerceAtLeast(0L)

        val holdProgress = when {
            step == null -> 0f
            phase == Phase.SUCCESS -> 1f
            step.type == StepType.HOLD -> (holdMs / (step.safeDuration * 1000f)).coerceIn(0f, 1f)
            step.type == StepType.PAUSE -> (phaseElapsed / (step.safeDuration * 1000f)).coerceIn(0f, 1f)
            else -> repsDone / step.safeReps.toFloat()
        }

        val itemFraction = when (phase) {
            Phase.SUCCESS, Phase.FINISHED -> 1f
            Phase.GET_READY -> 0f
            else -> if (step == null) 0f else when (step.type) {
                StepType.HOLD -> (repsDone + holdProgress) / step.safeReps
                StepType.INSTANT -> repsDone / step.safeReps.toFloat()
                StepType.PAUSE -> holdProgress
            }
        }
        val progress = if (plan.isEmpty() || phase == Phase.FINISHED) 1f
        else ((index + itemFraction) / plan.size).coerceIn(0f, 1f)

        return State(
            phase = phase,
            index = index,
            item = item,
            phaseElapsedMs = phaseElapsed,
            remainingMs = remaining,
            holdProgress = holdProgress,
            repsDone = repsDone,
            repsTarget = step?.safeReps ?: 0,
            matched = matched || phase == Phase.SUCCESS,
            score = smoothed,
            combo = combo,
            progress = progress,
            totalElapsedMs = totalElapsed,
            repEvents = repEvents,
            successEvents = successEvents
        )
    }

    fun report(): Report {
        val poseCount = plan.count { it.step.type != StepType.PAUSE }
        val successCount = results.count { it.success }
        val accuracy = if (results.isEmpty()) 0f else results.map { it.quality }.average().toFloat()
        val completion = if (poseCount == 0) 1f else successCount / poseCount.toFloat()
        val stars = when {
            poseCount == 0 -> 3
            completion >= 1f && accuracy >= 0.85f -> 3
            completion >= 0.8f && accuracy >= 0.75f -> 2
            completion > 0f -> 1
            else -> 0
        }
        return Report(
            exerciseName = exercise.name,
            results = results.toList(),
            totalMs = totalElapsed,
            accuracy = accuracy,
            maxCombo = maxCombo,
            stars = stars,
            successCount = successCount,
            poseCount = poseCount,
            totalReps = results.sumOf { it.repsDone }
        )
    }
}

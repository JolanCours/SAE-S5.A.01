package sae.app.sport.ui.creation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import sae.app.sport.model.Difficulty
import sae.app.sport.model.Exercise
import sae.app.sport.model.ExerciseStep
import sae.app.sport.model.RecordedPose
import sae.app.sport.model.StepType
import sae.app.sport.pose.PoseMath
import java.util.UUID

class EditorState(private val base: Exercise, fromBuiltIn: Boolean) {
    var name by mutableStateOf(if (fromBuiltIn) "${base.name} (perso)" else base.name)
    var description by mutableStateOf(base.description)
    var emoji by mutableStateOf(base.emoji)
    var colorIndex by mutableStateOf(base.colorIndex)
    var rounds by mutableStateOf(base.safeRounds)
    var difficulty by mutableStateOf(base.safeDifficulty)

    val steps = mutableStateListOf<ExerciseStep>().apply {
        addAll(if (fromBuiltIn) base.steps.map { it.copy(id = UUID.randomUUID().toString()) } else base.steps)
    }
    var selectedId by mutableStateOf(steps.firstOrNull()?.id)

    val originals = mutableStateMapOf<String, RecordedPose>().apply {
        steps.forEach { s -> s.pose?.let { put(s.id, it) } }
    }

    var dirty by mutableStateOf(fromBuiltIn)
        private set

    private val undoStack = ArrayDeque<Pair<List<ExerciseStep>, String?>>()
    var canUndo by mutableStateOf(false)
        private set

    private val exerciseId = if (fromBuiltIn) UUID.randomUUID().toString() else base.id

    val selected: ExerciseStep? get() = steps.firstOrNull { it.id == selectedId }
    val selectedIndex: Int get() = steps.indexOfFirst { it.id == selectedId }
    val poseCount: Int get() = steps.count { it.isPose && it.pose != null }

    fun estimatedTotalSec(): Int =
        (steps.sumOf { it.estimatedSec().toDouble() } * rounds.coerceAtLeast(1)).toInt()

    fun markDirty() {
        dirty = true
    }

    fun checkpoint() {
        undoStack.addLast(steps.toList() to selectedId)
        while (undoStack.size > 50) undoStack.removeFirst()
        canUndo = true
        dirty = true
    }

    fun undo() {
        val (prev, sel) = undoStack.removeLastOrNull() ?: return
        steps.clear()
        steps.addAll(prev)
        selectedId = sel?.takeIf { id -> prev.any { it.id == id } } ?: prev.firstOrNull()?.id
        canUndo = undoStack.isNotEmpty()
    }

    fun update(id: String, record: Boolean = true, transform: (ExerciseStep) -> ExerciseStep) {
        val i = steps.indexOfFirst { it.id == id }
        if (i < 0) return
        val updated = transform(steps[i])
        if (updated == steps[i]) return
        if (record) checkpoint() else dirty = true
        steps[i] = updated
    }

    private fun insertionIndex(): Int = if (selectedIndex >= 0) selectedIndex + 1 else steps.size

    fun addPoses(poses: List<RecordedPose>) {
        if (poses.isEmpty()) return
        checkpoint()
        var at = insertionIndex()
        var n = poseCount
        var firstId: String? = null
        for (p in poses) {
            n++
            val step = ExerciseStep(type = StepType.HOLD, pose = p, durationSec = 3f, name = "Pose $n")
            originals[step.id] = p
            steps.add(at++, step)
            if (firstId == null) firstId = step.id
        }
        selectedId = firstId
    }

    fun addTemplate(name: String, pose: RecordedPose) {
        checkpoint()
        val step = ExerciseStep(type = StepType.HOLD, pose = pose, durationSec = 3f, name = name)
        originals[step.id] = pose
        steps.add(insertionIndex(), step)
        selectedId = step.id
    }

    fun addPause() {
        checkpoint()
        val step = ExerciseStep(type = StepType.PAUSE, durationSec = 10f, name = "Pause")
        steps.add(insertionIndex(), step)
        selectedId = step.id
    }

    fun replacePose(id: String, pose: RecordedPose) {
        update(id) { it.copy(pose = pose) }
        originals[id] = pose
    }

    fun duplicateSelected() {
        val s = selected ?: return
        checkpoint()
        val copy = s.copy(id = UUID.randomUUID().toString())
        originals[s.id]?.let { originals[copy.id] = it }
        steps.add(selectedIndex + 1, copy)
        selectedId = copy.id
    }

    fun deleteSelected() {
        val i = selectedIndex
        if (i < 0) return
        checkpoint()
        steps.removeAt(i)
        selectedId = steps.getOrNull(i)?.id ?: steps.getOrNull(i - 1)?.id
    }

    fun moveSelected(delta: Int) {
        val i = selectedIndex
        val j = i + delta
        if (i < 0 || j !in steps.indices) return
        checkpoint()
        swap(i, j)
    }

    fun swap(i: Int, j: Int) {
        if (i !in steps.indices || j !in steps.indices) return
        val a = steps[i]
        steps[i] = steps[j]
        steps[j] = a
        dirty = true
    }

    fun mirrorSelected() {
        val s = selected ?: return
        val p = s.pose ?: return
        update(s.id) { it.copy(pose = PoseMath.mirror(p)) }
    }

    fun resetSelected() {
        val s = selected ?: return
        val original = originals[s.id] ?: return
        update(s.id) { it.copy(pose = original) }
    }

    fun toExercise(): Exercise = Exercise(
        id = exerciseId,
        name = name.trim().ifBlank { "Mon exercice" },
        description = description.trim(),
        emoji = emoji,
        colorIndex = colorIndex,
        steps = steps.toList(),
        rounds = rounds.coerceIn(1, 50),
        difficulty = difficulty,
        builtIn = false
    )

    companion object {
        val EMOJIS = listOf("💪", "🦵", "🧘", "🏃", "⭐", "🔥", "🤸", "🥊", "🏋️", "⚡", "☀️", "🎯", "🕺", "🧗")
        val DIFFICULTIES = Difficulty.values().toList()
    }
}

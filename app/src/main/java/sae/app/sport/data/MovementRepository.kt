package sae.app.sport.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import sae.app.sport.model.Exercise
import sae.app.sport.model.ExerciseStep
import sae.app.sport.model.MovementSequence
import sae.app.sport.model.StepType

class MovementRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("movement_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private companion object {
        const val KEY_LEGACY = "key_movements"
        const val KEY_EXERCISES = "key_exercises_v2"
    }

    fun getAllExercises(): List<Exercise> = BuiltInExercises.all + getCustomExercises()

    fun getExercise(id: String): Exercise? = getAllExercises().firstOrNull { it.id == id }

    fun getCustomExercises(): List<Exercise> {
        migrateLegacyIfNeeded()
        val json = prefs.getString(KEY_EXERCISES, null) ?: return emptyList()
        val type = object : TypeToken<List<Exercise>>() {}.type
        return try {
            val list: List<Exercise?> = gson.fromJson(json, type) ?: emptyList()
            list.filterNotNull()
                .map { it.sanitized().copy(builtIn = false) }
                .sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveExercise(exercise: Exercise) {
        val list = getCustomExercises().toMutableList()
        list.removeAll { it.id == exercise.id }
        list.add(exercise.copy(builtIn = false, updatedAt = System.currentTimeMillis()))
        write(list)
    }

    fun deleteExercise(id: String) {
        val list = getCustomExercises().toMutableList()
        list.removeAll { it.id == id }
        write(list)
    }

    private fun write(list: List<Exercise>) {
        prefs.edit().putString(KEY_EXERCISES, gson.toJson(list)).apply()
    }

    private fun migrateLegacyIfNeeded() {
        if (prefs.contains(KEY_EXERCISES) || !prefs.contains(KEY_LEGACY)) return
        val json = prefs.getString(KEY_LEGACY, null)
        val legacy: List<MovementSequence> = try {
            val type = object : TypeToken<List<MovementSequence>>() {}.type
            gson.fromJson<List<MovementSequence>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        val converted = legacy.mapIndexed { i, seq ->
            Exercise(
                id = seq.id,
                name = seq.name.ifBlank { "Mouvement ${i + 1}" },
                emoji = "🤸",
                colorIndex = i,
                steps = seq.poses.mapIndexed { j, pose ->
                    ExerciseStep(type = StepType.HOLD, pose = pose, durationSec = 2f, name = "Pose ${j + 1}")
                },
                updatedAt = System.currentTimeMillis() - i
            )
        }
        prefs.edit()
            .putString(KEY_EXERCISES, gson.toJson(converted))
            .remove(KEY_LEGACY)
            .apply()
    }
}

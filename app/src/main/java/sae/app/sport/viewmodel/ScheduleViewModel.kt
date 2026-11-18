package sae.app.sport.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import sae.app.sport.model.DailySession
import sae.app.sport.model.Exercise
import java.time.LocalDate

class ScheduleViewModel : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _sessions = MutableStateFlow<Map<LocalDate, DailySession>>(emptyMap())
    val sessions: StateFlow<Map<LocalDate, DailySession>> = _sessions.asStateFlow()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addExercise(date: LocalDate, name: String, sets: Int, reps: Int) {
        val exercise = Exercise(name = name, sets = sets, reps = reps)
        _sessions.update { currentSessions ->
            val existingSession = currentSessions[date] ?: DailySession(date = date)
            val updatedExercises = existingSession.exercises + exercise
            val updatedSession = existingSession.copy(exercises = updatedExercises)
            currentSessions + (date to updatedSession)
        }
    }

    fun toggleSessionCompletion(date: LocalDate) {
        _sessions.update { currentSessions ->
            val existingSession = currentSessions[date] ?: DailySession(date = date)
            val updatedSession = existingSession.copy(isCompleted = !existingSession.isCompleted)
            currentSessions + (date to updatedSession)
        }
    }
}

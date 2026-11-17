package sae.app.sport.model

import java.time.LocalDate

data class DailySession(
    val date: LocalDate,
    val exercises: List<Exercise> = emptyList(),
    val isCompleted: Boolean = false
)

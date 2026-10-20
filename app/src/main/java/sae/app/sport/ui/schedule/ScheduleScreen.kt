package sae.app.sport.ui.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import sae.app.sport.viewmodel.ScheduleViewModel

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val session = sessions[selectedDate]

    Column(modifier = Modifier.fillMaxSize()) {

        Spacer(modifier = Modifier.height(100.dp))

        CalendarView(
            selectedDate = selectedDate,
            sessions = sessions,
            onDateSelected = { date -> viewModel.selectDate(date) }
        )

        DayDetailsView(
            date = selectedDate,
            session = session,
            onAddExercise = { name, sets, reps ->
                viewModel.addExercise(selectedDate, name, sets, reps)
            },
            onToggleCompletion = {
                viewModel.toggleSessionCompletion(selectedDate)
            }
        )
    }
}

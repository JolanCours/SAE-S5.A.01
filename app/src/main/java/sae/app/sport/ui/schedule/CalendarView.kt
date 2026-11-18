package sae.app.sport.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*


import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import sae.app.sport.model.DailySession
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

@Composable
fun CalendarView(
    selectedDate: LocalDate,
    sessions: Map<LocalDate, DailySession>,
    onDateSelected: (LocalDate) -> Unit
) {
    val startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val startOfCalendar = startOfWeek.minusWeeks(1)
    

    val dates = remember(selectedDate) {
        (0..20).map { startOfCalendar.plusDays(it.toLong()) }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        val daysOfWeek = (0..6).map { startOfWeek.plusDays(it.toLong()).dayOfWeek }
        
        items(daysOfWeek) { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(dates) { date ->
            val isSelected = date == selectedDate
            val session = sessions[date]
            val isPast = date.isBefore(LocalDate.now())
            val isToday = date.isEqual(LocalDate.now())

            val statusColor = when {
                session?.isCompleted == true -> Color(0xFF4CAF50) 
                isPast && (session == null || !session.isCompleted) -> Color(0xFFF44336) 
                else -> Color.Transparent
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable { onDateSelected(date) }
                    .padding(2.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(statusColor)
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (statusColor != Color.Transparent) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

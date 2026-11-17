package sae.app.sport.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import sae.app.sport.model.PoseExercise
import sae.app.sport.model.ExerciseStep
import sae.app.sport.model.StepType
import sae.app.sport.model.formatDuration
import sae.app.sport.ui.components.AppIcon
import sae.app.sport.ui.components.DuoButton
import sae.app.sport.ui.components.DuoCard
import sae.app.sport.ui.components.DuoIconButton
import sae.app.sport.ui.components.DuoStyle
import sae.app.sport.ui.components.PoseThumbnail
import sae.app.sport.ui.components.SectionHeader
import sae.app.sport.ui.components.SimpleIcon
import sae.app.sport.ui.components.color
import sae.app.sport.ui.components.emoji
import sae.app.sport.ui.components.summary
import sae.app.sport.ui.theme.AppTheme
import sae.app.sport.ui.theme.Red
import sae.app.sport.ui.theme.RedDark
import sae.app.sport.ui.theme.cardColors

@Composable
fun ExerciseDetailScreen(
    exercise: PoseExercise,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val c = AppTheme.colors
    var confirmDelete by remember { mutableStateOf(false) }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        val wide = maxWidth >= 720.dp
        if (wide) {
            Row(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    Header(exercise, onBack)
                    Column(Modifier.padding(20.dp)) {
                        Actions(exercise, onStart, onEdit) { confirmDelete = true }
                    }
                }
                Column(
                    Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    StepsList(exercise)
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Header(exercise, onBack)
                    Column(
                        Modifier
                            .padding(20.dp)
                            .widthIn(max = 640.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        StepsList(exercise)
                    }
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(c.surface)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .widthIn(max = 640.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Actions(exercise, onStart, onEdit) { confirmDelete = true }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = c.surface,
            title = { Text("Supprimer « ${exercise.name} » ?", style = MaterialTheme.typography.headlineSmall, color = c.text) },
            text = { Text("Cette action est définitive.", style = MaterialTheme.typography.bodyLarge, color = c.textMuted) },
            confirmButton = {
                Column(Modifier.fillMaxWidth()) {
                    DuoButton("Supprimer", onClick = { confirmDelete = false; onDelete() }, style = DuoStyle.DANGER)
                    Spacer(Modifier.height(8.dp))
                    DuoButton("Annuler", onClick = { confirmDelete = false }, style = DuoStyle.OUTLINE)
                }
            }
        )
    }
}

@Composable
private fun Header(exercise: PoseExercise, onBack: () -> Unit) {
    val (face, shadow) = cardColors(exercise.colorIndex)
    Box(
        Modifier
            .fillMaxWidth()
            .background(shadow)
            .padding(bottom = 6.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(face)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(SimpleIcon.BACK, Color.White, size = 24.dp)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(exercise.emoji, style = MaterialTheme.typography.headlineLarge)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(exercise.name.ifBlank { "Sans nom" }, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    if (exercise.description.isNotBlank()) {
                        Text(exercise.description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderChip(exercise.safeDifficulty.label)
                HeaderChip("~${formatDuration(exercise.estimatedDurationSec())}")
                if (exercise.safeRounds > 1) HeaderChip("× ${exercise.safeRounds} tours")
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun HeaderChip(text: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.25f))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, color = Color.White)
    }
}

@Composable
private fun ColumnScope.Actions(exercise: PoseExercise, onStart: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    DuoButton(
        "Commencer",
        onClick = onStart,
        enabled = exercise.poseStepCount > 0,
        height = 56.dp
    )
    Spacer(Modifier.height(10.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        DuoButton(
            if (exercise.builtIn) "Personnaliser" else "Modifier",
            onClick = onEdit,
            style = DuoStyle.OUTLINE,
            modifier = Modifier.weight(1f)
        )
        if (!exercise.builtIn) {
            Spacer(Modifier.width(10.dp))
            DuoIconButton(onClick = onDelete, size = 52.dp, face = Red, shadow = RedDark, bordered = false) {
                AppIcon(SimpleIcon.TRASH, Color.White, size = 24.dp)
            }
        }
    }
}

@Composable
private fun StepsList(exercise: PoseExercise) {
    val c = AppTheme.colors
    SectionHeader("Enchaînement · ${exercise.steps.size} étape${if (exercise.steps.size > 1) "s" else ""}")
    Spacer(Modifier.height(12.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        exercise.steps.forEachIndexed { i, step ->
            StepRow(i, step)
        }
    }
    if (exercise.safeRounds > 1) {
        Spacer(Modifier.height(12.dp))
        Text(
            "🔁 L'enchaînement est répété ${exercise.safeRounds} fois.",
            style = MaterialTheme.typography.bodyMedium,
            color = c.textMuted
        )
    }
}

@Composable
private fun StepRow(index: Int, step: ExerciseStep) {
    val c = AppTheme.colors
    val color = step.type.color()
    DuoCard {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${index + 1}",
                style = MaterialTheme.typography.titleMedium,
                color = c.textMuted,
                modifier = Modifier.width(24.dp)
            )
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (step.type == StepType.PAUSE) {
                    Text(step.type.emoji(), style = MaterialTheme.typography.headlineSmall)
                } else {
                    PoseThumbnail(step.pose, Modifier.fillMaxSize(), color = color)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(step.displayName(index), style = MaterialTheme.typography.titleMedium, color = c.text, maxLines = 1)
                Text(step.summary(), style = MaterialTheme.typography.labelLarge, color = color)
            }
        }
    }
}

package sae.app.sport.ui.creation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import sae.app.sport.model.ExerciseStep
import sae.app.sport.model.StepType
import sae.app.sport.model.formatDuration
import sae.app.sport.ui.components.AppIcon
import sae.app.sport.ui.components.DuoIconButton
import sae.app.sport.ui.components.DuoPressable
import sae.app.sport.ui.components.PoseThumbnail
import sae.app.sport.ui.components.SimpleIcon
import sae.app.sport.ui.components.color
import sae.app.sport.ui.components.emoji
import sae.app.sport.ui.components.shadow
import sae.app.sport.ui.components.summary
import sae.app.sport.ui.theme.AppTheme
import sae.app.sport.ui.theme.Blue
import sae.app.sport.ui.theme.BlueDark
import sae.app.sport.ui.theme.Green
import sae.app.sport.ui.theme.GreenDark
import sae.app.sport.ui.theme.Purple
import sae.app.sport.ui.theme.PurpleDark
import sae.app.sport.ui.theme.Red

private val CLIP_HEIGHT = 104.dp
private val CLIP_GAP = 8.dp

private fun clipWidth(step: ExerciseStep): Dp = (step.estimatedSec() * 11f).dp.coerceIn(78.dp, 210.dp)

@Composable
fun EditorTimeline(
    state: EditorState,
    onAddPoseCamera: () -> Unit,
    onAddTemplate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = AppTheme.colors
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

    LaunchedEffect(state.selectedId) {
        val idx = state.selectedIndex
        if (idx < 0 || draggingId != null) return@LaunchedEffect
        val before = with(density) {
            state.steps.take(idx).sumOf { (clipWidth(it) + CLIP_GAP).toPx().toDouble() }.toFloat()
        }
        val target = (before - with(density) { 40.dp.toPx() }).coerceAtLeast(0f)
        scroll.animateScrollTo(target.toInt())
    }

    Column(
        modifier
            .fillMaxWidth()
            .background(c.surfaceAlt)
            .padding(top = 10.dp, bottom = 8.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TIMELINE", style = MaterialTheme.typography.labelLarge, color = c.textMuted)
            Spacer(Modifier.width(8.dp))
            Text(
                "≈ ${formatDuration(state.estimatedTotalSec())}",
                style = MaterialTheme.typography.labelLarge,
                color = c.text
            )
            Spacer(Modifier.weight(1f))
            Text("🔁 Tours", style = MaterialTheme.typography.labelLarge, color = c.textMuted)
            Spacer(Modifier.width(6.dp))
            MiniStepper(
                value = state.rounds,
                onMinus = { if (state.rounds > 1) { state.rounds--; state.markDirty() } },
                onPlus = { if (state.rounds < 50) { state.rounds++; state.markDirty() } }
            )
        }
        Spacer(Modifier.height(8.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(CLIP_GAP),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (step in state.steps) {
                key(step.id) {
                    val isDragging = draggingId == step.id
                    val index = state.steps.indexOf(step)
                    Clip(
                        step = step,
                        index = index,
                        selected = step.id == state.selectedId,
                        dragging = isDragging,
                        modifier = Modifier
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                translationX = if (isDragging) dragOffset else 0f
                                val s = if (isDragging) 1.06f else 1f
                                scaleX = s
                                scaleY = s
                            }
                            .pointerInput(step.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        state.checkpoint()
                                        state.selectedId = step.id
                                        draggingId = step.id
                                        dragOffset = 0f
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOffset += amount.x
                                        val i = state.steps.indexOfFirst { it.id == step.id }
                                        if (dragOffset > 0 && i < state.steps.lastIndex) {
                                            val next = (clipWidth(state.steps[i + 1]) + CLIP_GAP).toPx()
                                            if (dragOffset > next / 2) {
                                                state.swap(i, i + 1)
                                                dragOffset -= next
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        } else if (dragOffset < 0 && i > 0) {
                                            val prev = (clipWidth(state.steps[i - 1]) + CLIP_GAP).toPx()
                                            if (-dragOffset > prev / 2) {
                                                state.swap(i, i - 1)
                                                dragOffset += prev
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        }
                                    },
                                    onDragEnd = { draggingId = null; dragOffset = 0f },
                                    onDragCancel = { draggingId = null; dragOffset = 0f }
                                )
                            },
                        onClick = { state.selectedId = step.id }
                    )
                }
            }
            Box(
                Modifier
                    .size(width = 64.dp, height = CLIP_HEIGHT)
                    .clip(RoundedCornerShape(14.dp))
                    .border(2.dp, c.border, RoundedCornerShape(14.dp))
                    .clickable(onClick = onAddPoseCamera),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(SimpleIcon.PLUS, c.textMuted, size = 28.dp)
            }
        }

        if (state.steps.size > 1) {
            Text(
                "Appui long sur un clip puis glisse pour le déplacer",
                style = MaterialTheme.typography.bodySmall,
                color = c.textMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolButton("Pose", SimpleIcon.CAMERA, Green, GreenDark, onAddPoseCamera)
            ToolButton("Modèle", SimpleIcon.PLUS, Blue, BlueDark, onAddTemplate)
            ToolButton("Pause", SimpleIcon.TIMER, Purple, PurpleDark) { state.addPause() }
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = 2.dp, height = 36.dp)
                    .background(c.border)
            )
            val hasSel = state.selected != null
            DuoIconButton(onClick = { state.moveSelected(-1) }, enabled = hasSel && state.selectedIndex > 0, size = 44.dp) {
                AppIcon(SimpleIcon.CHEVRON_LEFT, c.text, size = 22.dp)
            }
            DuoIconButton(onClick = { state.moveSelected(1) }, enabled = hasSel && state.selectedIndex < state.steps.lastIndex, size = 44.dp) {
                AppIcon(SimpleIcon.CHEVRON_RIGHT, c.text, size = 22.dp)
            }
            DuoIconButton(onClick = { state.duplicateSelected() }, enabled = hasSel, size = 44.dp) {
                AppIcon(SimpleIcon.COPY, c.text, size = 22.dp)
            }
            DuoIconButton(onClick = { state.deleteSelected() }, enabled = hasSel, size = 44.dp) {
                AppIcon(SimpleIcon.TRASH, if (hasSel) Red else c.textMuted, size = 22.dp)
            }
            DuoIconButton(onClick = { state.undo() }, enabled = state.canUndo, size = 44.dp) {
                AppIcon(SimpleIcon.RESET, c.text, size = 22.dp)
            }
        }
    }
}

@Composable
private fun ToolButton(label: String, icon: SimpleIcon, face: Color, shadow: Color, onClick: () -> Unit) {
    DuoPressable(
        onClick = onClick,
        face = face,
        shadow = shadow,
        shape = RoundedCornerShape(14.dp),
        fillWidth = false,
        minHeight = 44.dp
    ) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            AppIcon(icon, Color.White, size = 20.dp)
            Spacer(Modifier.width(6.dp))
            Text(label.uppercase(), style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}

@Composable
fun MiniStepper(value: Int, onMinus: () -> Unit, onPlus: () -> Unit, suffix: String = "") {
    val c = AppTheme.colors
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(c.surface)
            .border(2.dp, c.border, RoundedCornerShape(50)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "−",
            style = MaterialTheme.typography.titleLarge,
            color = c.text,
            modifier = Modifier
                .clickable(onClick = onMinus)
                .padding(horizontal = 12.dp, vertical = 2.dp)
        )
        Text("$value$suffix", style = MaterialTheme.typography.titleMedium, color = c.text)
        Text(
            "+",
            style = MaterialTheme.typography.titleLarge,
            color = c.text,
            modifier = Modifier
                .clickable(onClick = onPlus)
                .padding(horizontal = 12.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun Clip(
    step: ExerciseStep,
    index: Int,
    selected: Boolean,
    dragging: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val c = AppTheme.colors
    val color = step.type.color()
    val width by animateDpAsState(clipWidth(step), label = "clipWidth")
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier
            .size(width = width, height = CLIP_HEIGHT)
            .clip(shape)
            .background(c.surface)
            .border(if (selected) 3.dp else 2.dp, if (selected) c.text else step.type.shadow(), shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(color)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${index + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
            Spacer(Modifier.width(4.dp))
            Text(
                step.summary(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(color.copy(alpha = if (dragging) 0.22f else 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            if (step.type == StepType.PAUSE) {
                Text(step.type.emoji(), style = MaterialTheme.typography.headlineSmall)
            } else {
                PoseThumbnail(step.pose, Modifier.fillMaxSize(), color = color, padding = 0.08f)
            }
        }
        Text(
            step.displayName(index),
            style = MaterialTheme.typography.labelMedium,
            color = c.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

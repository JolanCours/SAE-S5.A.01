package sae.app.sport.ui.creation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sae.app.sport.model.Exercise
import sae.app.sport.model.StepType
import sae.app.sport.ui.components.AppIcon
import sae.app.sport.ui.components.DuoButton
import sae.app.sport.ui.components.DuoCard
import sae.app.sport.ui.components.DuoIconButton
import sae.app.sport.ui.components.DuoStyle
import sae.app.sport.ui.components.SimpleIcon
import sae.app.sport.ui.components.color
import sae.app.sport.ui.components.emoji
import sae.app.sport.ui.components.formatSec
import sae.app.sport.ui.components.label
import sae.app.sport.ui.components.shadow
import sae.app.sport.ui.theme.AppTheme
import sae.app.sport.ui.theme.Purple
import kotlin.math.roundToInt

private sealed interface CaptureMode {
    object Add : CaptureMode
    data class Replace(val stepId: String) : CaptureMode
}

@Composable
fun EditorScreen(
    initial: Exercise,
    fromBuiltIn: Boolean,
    onSave: (Exercise) -> Unit,
    onCancel: () -> Unit
) {
    val state = remember(initial.id) { EditorState(initial, fromBuiltIn) }
    var capture by remember { mutableStateOf<CaptureMode?>(null) }
    var showTemplates by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }

    val mode = capture
    if (mode != null) {
        PoseCaptureScreen(
            single = mode is CaptureMode.Replace,
            onDone = { poses ->
                when (mode) {
                    is CaptureMode.Replace -> poses.firstOrNull()?.let { state.replacePose(mode.stepId, it) }
                    CaptureMode.Add -> state.addPoses(poses)
                }
                capture = null
            },
            onCancel = { capture = null }
        )
        return
    }

    BackHandler {
        if (state.dirty) confirmDiscard = true else onCancel()
    }

    EditorContent(
        state = state,
        onClose = { if (state.dirty) confirmDiscard = true else onCancel() },
        onSave = { onSave(state.toExercise()) },
        onSettings = { showSettings = true },
        onAddCamera = { capture = CaptureMode.Add },
        onAddTemplate = { showTemplates = true },
        onRecapture = { id -> capture = CaptureMode.Replace(id) }
    )

    if (showTemplates) {
        TemplatesDialog(
            onPick = { name, pose -> state.addTemplate(name, pose); showTemplates = false },
            onDismiss = { showTemplates = false }
        )
    }
    if (showSettings) {
        SettingsDialog(state, onDismiss = { showSettings = false })
    }
    if (confirmDiscard) {
        DiscardDialog(
            onKeepEditing = { confirmDiscard = false },
            onDiscard = { confirmDiscard = false; onCancel() }
        )
    }
}

@Composable
private fun EditorContent(
    state: EditorState,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onSettings: () -> Unit,
    onAddCamera: () -> Unit,
    onAddTemplate: () -> Unit,
    onRecapture: (String) -> Unit
) {
    val c = AppTheme.colors
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        val wide = maxWidth >= 700.dp
        Column(Modifier.fillMaxSize()) {
            EditorTopBar(state, onClose, onSave, onSettings)
            if (wide) {
                Row(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Stage(state, onAddCamera, onAddTemplate, onRecapture, Modifier.weight(1f).fillMaxHeight())
                    Column(
                        Modifier
                            .width(360.dp)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Inspector(state)
                    }
                }
            } else {
                Stage(
                    state, onAddCamera, onAddTemplate, onRecapture,
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                if (state.selected != null) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 230.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Inspector(state)
                    }
                }
            }
            EditorTimeline(state, onAddPoseCamera = onAddCamera, onAddTemplate = onAddTemplate)
        }
    }
}

@Composable
private fun EditorTopBar(state: EditorState, onClose: () -> Unit, onSave: () -> Unit, onSettings: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) { AppIcon(SimpleIcon.CLOSE, c.textMuted, size = 24.dp) }
        Spacer(Modifier.width(6.dp))
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(c.surfaceAlt)
                .clickable(onClick = onSettings),
            contentAlignment = Alignment.Center
        ) { Text(state.emoji, style = MaterialTheme.typography.titleLarge) }
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            BasicTextField(
                value = state.name,
                onValueChange = { state.name = it.take(40); state.markDirty() },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(color = c.text),
                cursorBrush = SolidColor(c.text),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                decorationBox = { inner ->
                    if (state.name.isEmpty()) {
                        Text("Nom de l'exercice", style = MaterialTheme.typography.titleLarge, color = c.textMuted)
                    }
                    inner()
                }
            )
        }
        Spacer(Modifier.width(8.dp))
        DuoButton(
            "Enregistrer",
            onClick = onSave,
            enabled = state.poseCount > 0,
            fillWidth = false,
            height = 42.dp
        )
    }
}

@Composable
private fun Stage(
    state: EditorState,
    onAddCamera: () -> Unit,
    onAddTemplate: () -> Unit,
    onRecapture: (String) -> Unit,
    modifier: Modifier
) {
    val c = AppTheme.colors
    val step = state.selected
    Box(modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        when {
            step == null -> EmptyStage(onAddCamera, onAddTemplate, state.steps.isEmpty())

            step.type == StepType.PAUSE -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Purple),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("☕", style = MaterialTheme.typography.displayMedium)
                        Text("Pause", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        Text(formatSec(step.safeDuration), style = MaterialTheme.typography.displaySmall, color = Color.White)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Passable d'un tap pendant la séance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            else -> {
                val pose = step.pose
                if (pose != null) {
                    PoseEditorCanvas(
                        pose = pose,
                        color = step.type.color(),
                        modifier = Modifier.fillMaxSize(),
                        onDragStart = { state.checkpoint() },
                        onPoseChange = { p -> state.update(step.id, record = false) { it.copy(pose = p) } }
                    )
                }
                Column(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DuoIconButton(onClick = { onRecapture(step.id) }, size = 44.dp) {
                        AppIcon(SimpleIcon.CAMERA, c.text, size = 22.dp)
                    }
                    DuoIconButton(onClick = { state.mirrorSelected() }, size = 44.dp) {
                        AppIcon(SimpleIcon.MIRROR, c.text, size = 22.dp)
                    }
                    DuoIconButton(
                        onClick = { state.resetSelected() },
                        enabled = state.originals[step.id] != null && state.originals[step.id] != pose,
                        size = 44.dp
                    ) {
                        AppIcon(SimpleIcon.RESET, c.text, size = 22.dp)
                    }
                }
                Text(
                    "Fais glisser les ronds pour ajuster la pose",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMuted,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyStage(onAddCamera: () -> Unit, onAddTemplate: () -> Unit, timelineEmpty: Boolean) {
    val c = AppTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(c.surfaceAlt)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎬", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            if (timelineEmpty) "Ta timeline est vide" else "Sélectionne un clip",
            style = MaterialTheme.typography.headlineSmall,
            color = c.text,
            textAlign = TextAlign.Center
        )
        Text(
            "Enregistre des poses avec la caméra ou pars d'un modèle, puis ajoute des pauses entre elles.",
            style = MaterialTheme.typography.bodyMedium,
            color = c.textMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        DuoButton("Enregistrer une pose", onClick = onAddCamera, fillWidth = false)
        Spacer(Modifier.height(8.dp))
        DuoButton("Choisir un modèle", onClick = onAddTemplate, fillWidth = false, style = DuoStyle.OUTLINE)
    }
}

@Composable
private fun Inspector(state: EditorState) {
    val c = AppTheme.colors
    val step = state.selected ?: return
    val id = step.id
    var sliding by remember(id) { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (step.isPose) {
            BasicTextField(
                value = step.name,
                onValueChange = { v -> state.update(id, record = false) { it.copy(name = v.take(30)) } },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(color = c.text),
                cursorBrush = SolidColor(c.text),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surfaceAlt)
                    .border(2.dp, c.border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                decorationBox = { inner ->
                    if (step.name.isEmpty()) Text("Nom de la pose", style = MaterialTheme.typography.titleMedium, color = c.textMuted)
                    inner()
                }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (t in listOf(StepType.HOLD, StepType.INSTANT)) {
                    DuoCard(
                        modifier = Modifier.weight(1f),
                        onClick = { state.update(id) { it.copy(type = t) } },
                        selected = step.type == t,
                        accent = t.color(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${t.emoji()} ${t.label()}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (step.type == t) t.shadow() else c.text,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }

        if (step.type == StepType.HOLD || step.type == StepType.PAUSE) {
            val accent = step.type.color()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (step.type == StepType.HOLD) "Temps à tenir" else "Durée de la pause",
                    style = MaterialTheme.typography.titleSmall,
                    color = c.text,
                    modifier = Modifier.weight(1f)
                )
                Text(formatSec(step.safeDuration), style = MaterialTheme.typography.titleMedium, color = accent)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepperButton("−") {
                    state.update(id) { it.copy(durationSec = (it.safeDuration - 0.5f).coerceAtLeast(0.5f)) }
                }
                Slider(
                    value = step.safeDuration.coerceIn(0.5f, 60f),
                    onValueChange = { v ->
                        if (!sliding) {
                            state.checkpoint()
                            sliding = true
                        }
                        val rounded = (v * 2).roundToInt() / 2f
                        state.update(id, record = false) { it.copy(durationSec = rounded.coerceAtLeast(0.5f)) }
                    },
                    onValueChangeFinished = { sliding = false },
                    valueRange = 0.5f..60f,
                    colors = SliderDefaults.colors(
                        thumbColor = accent,
                        activeTrackColor = accent,
                        inactiveTrackColor = c.border
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                StepperButton("+") {
                    state.update(id) { it.copy(durationSec = (it.safeDuration + 0.5f).coerceAtMost(600f)) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(3f, 5f, 10f, 20f, 30f).forEach { s ->
                    val sel = step.safeDuration == s
                    Text(
                        formatSec(s),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (sel) Color.White else c.textMuted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (sel) accent else c.surfaceAlt)
                            .clickable { state.update(id) { it.copy(durationSec = s) } }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        if (step.isPose) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Répétitions", style = MaterialTheme.typography.titleSmall, color = c.text)
                    Text(
                        if (step.type == StepType.INSTANT) "Sortir de la pose entre deux reps"
                        else "Petite pause de relâche entre deux séries",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMuted
                    )
                }
                MiniStepper(
                    value = step.safeReps,
                    onMinus = { if (step.safeReps > 1) state.update(id) { it.copy(reps = it.safeReps - 1) } },
                    onPlus = { if (step.safeReps < 99) state.update(id) { it.copy(reps = it.safeReps + 1) } }
                )
            }
        }

        Text(
            text = when (step.type) {
                StepType.HOLD -> "⏱️ Il faut tenir la pose ${formatSec(step.safeDuration)}. Le chrono s'arrête si on la perd."
                StepType.INSTANT -> "⚡ Il suffit de toucher la pose, le plus vite possible."
                StepType.PAUSE -> "☕ Pendant la séance, la pause peut être passée d'un tap."
            },
            style = MaterialTheme.typography.bodySmall,
            color = c.textMuted
        )
    }
}

@Composable
private fun StepperButton(text: String, onClick: () -> Unit) {
    val c = AppTheme.colors
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .border(2.dp, c.border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.titleLarge, color = c.text)
    }
}

package sae.app.sport.ui.training

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import sae.app.sport.model.Exercise
import sae.app.sport.model.RecordedPose
import sae.app.sport.model.StepType
import sae.app.sport.pose.PoseMath
import sae.app.sport.training.WorkoutEngine
import sae.app.sport.training.WorkoutEngine.Phase
import sae.app.sport.ui.camera.CameraPermissionGate
import sae.app.sport.ui.camera.LiveFrame
import sae.app.sport.ui.camera.PoseCameraPreview
import sae.app.sport.ui.components.AppIcon
import sae.app.sport.ui.components.DuoButton
import sae.app.sport.ui.components.DuoCard
import sae.app.sport.ui.components.DuoProgressBar
import sae.app.sport.ui.components.DuoStyle
import sae.app.sport.ui.components.Pill
import sae.app.sport.ui.components.PoseMapper
import sae.app.sport.ui.components.PoseThumbnail
import sae.app.sport.ui.components.SimpleIcon
import sae.app.sport.ui.components.color
import sae.app.sport.ui.components.drawFigure
import sae.app.sport.ui.components.fillCenterMapper
import sae.app.sport.ui.components.instruction
import sae.app.sport.ui.components.praise
import sae.app.sport.ui.components.shadow
import sae.app.sport.ui.theme.AppTheme
import sae.app.sport.ui.theme.Blue
import sae.app.sport.ui.theme.Green
import sae.app.sport.ui.theme.GreenDark
import sae.app.sport.ui.theme.GreenLight
import sae.app.sport.ui.theme.Orange
import sae.app.sport.ui.theme.Purple
import sae.app.sport.ui.theme.Red
import sae.app.sport.ui.theme.Yellow
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.ceil
import kotlin.math.sqrt

@Composable
fun TrainingSessionScreen(
    exercise: Exercise,
    onQuit: () -> Unit,
    onFinished: (WorkoutEngine.Report) -> Unit
) {
    CameraPermissionGate {
        SessionContent(exercise, onQuit, onFinished)
    }
}

@Composable
private fun SessionContent(
    exercise: Exercise,
    onQuit: () -> Unit,
    onFinished: (WorkoutEngine.Report) -> Unit
) {
    val engine = remember(exercise.id) { WorkoutEngine(exercise) }
    var ui by remember { mutableStateOf(engine.state()) }
    val latestFrame = remember { AtomicReference<LiveFrame?>(null) }
    var frame by remember { mutableStateOf<LiveFrame?>(null) }
    var match by remember { mutableStateOf<PoseMath.MatchResult?>(null) }
    var modelError by remember { mutableStateOf<String?>(null) }
    var frontCamera by rememberSaveable { mutableStateOf(true) }
    var showQuit by remember { mutableStateOf(false) }
    val finished by rememberUpdatedState(onFinished)

    BackHandler { showQuit = true }

    LaunchedEffect(engine) {
        var last = withFrameMillis { it }
        while (!engine.isFinished) {
            val now = withFrameMillis { it }
            val dt = now - last
            last = now
            val f = latestFrame.get()?.takeIf { it.isFresh() }
            val step = engine.currentStep
            val target = step?.pose
            val livePose = f?.pose
            val m = if (livePose != null && step != null && target != null && step.type != StepType.PAUSE) {
                PoseMath.compare(livePose, target)
            } else null
            engine.tick(dt, m?.score)
            ui = engine.state()
            frame = f
            match = m
        }
        delay(450)
        finished(engine.report())
    }

    SessionFeedback(ui)

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val wide = maxWidth > maxHeight && maxWidth >= 600.dp

        PoseCameraPreview(
            modifier = Modifier.fillMaxSize(),
            frontCamera = frontCamera,
            onFrame = { latestFrame.set(it) },
            onModelError = { modelError = it }
        )

        PoseOverlay(
            modifier = Modifier.fillMaxSize(),
            state = ui,
            frame = frame,
            match = match
        )

        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            TopBar(
                progress = ui.progress,
                combo = ui.combo,
                onClose = { showQuit = true },
                onFlipCamera = { frontCamera = !frontCamera }
            )
            val target = ui.item?.step?.pose
            if (target != null && ui.item?.step?.type != StepType.PAUSE && ui.phase != Phase.GET_READY) {
                Spacer(Modifier.height(12.dp))
                TargetCard(target, ui.item!!.step.type.color(), Modifier.align(if (wide) Alignment.Start else Alignment.End))
            }
        }

        if (ui.phase != Phase.GET_READY && ui.phase != Phase.FINISHED && ui.item?.step?.type != StepType.PAUSE) {
            if (wide) {
                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .width(360.dp)
                ) {
                    SessionPanel(engine, ui, match, rounded = RoundedCornerShape(24.dp), onSkip = { engine.skip() })
                }
            } else {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    SessionPanel(
                        engine, ui, match,
                        rounded = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        onSkip = { engine.skip() },
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = ui.phase == Phase.GET_READY,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            GetReadyOverlay(ui, frame, onSkip = { engine.skip() })
        }

        AnimatedVisibility(
            visible = ui.phase == Phase.ACTIVE && ui.item?.step?.type == StepType.PAUSE,
            enter = fadeIn() + slideInVertically { it / 6 },
            exit = fadeOut() + slideOutVertically { it / 6 },
            modifier = Modifier.fillMaxSize()
        ) {
            PauseOverlay(engine, ui, onSkip = { engine.skip() })
        }

        modelError?.let { err ->
            Box(
                Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Red)
                    .padding(16.dp)
            ) {
                Text(err, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    if (showQuit) {
        QuitDialog(onDismiss = { showQuit = false }, onQuit = onQuit)
    }
}

@Composable
private fun SessionFeedback(ui: WorkoutEngine.State) {
    val haptic = LocalHapticFeedback.current
    val tone = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            null
        }
    }
    DisposableEffect(Unit) { onDispose { tone?.release() } }

    LaunchedEffect(ui.repEvents) {
        if (ui.repEvents > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        }
    }
    LaunchedEffect(ui.successEvents) {
        if (ui.successEvents > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            tone?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
        }
    }
}

private fun boneColor(score: Float?): Color? = when {
    score == null -> null
    score >= 0.75f -> Green
    score >= 0.45f -> Yellow
    else -> Red
}

private fun torsoPx(pose: RecordedPose, mapper: PoseMapper): Float {
    val l = pose.landmarks
    val sh = mapper.map((l[11].x + l[12].x) / 2f, (l[11].y + l[12].y) / 2f)
    val hip = mapper.map((l[23].x + l[24].x) / 2f, (l[23].y + l[24].y) / 2f)
    val dx = sh.x - hip.x
    val dy = sh.y - hip.y
    return sqrt(dx * dx + dy * dy)
}

@Composable
private fun PoseOverlay(
    modifier: Modifier,
    state: WorkoutEngine.State,
    frame: LiveFrame?,
    match: PoseMath.MatchResult?
) {
    val step = state.item?.step
    val ghostColor by animateColorAsState(
        targetValue = if (state.matched) Green else (step?.type?.color() ?: Blue),
        label = "ghost"
    )
    Canvas(modifier) {
        val live = frame?.pose?.takeIf { PoseMath.isValid(it) }
        val target = if (step != null && step.type != StepType.PAUSE) step.pose?.takeIf { PoseMath.isValid(it) } else null
        if (state.phase == Phase.GET_READY || state.phase == Phase.FINISHED) {
            if (live != null) {
                val m = fillCenterMapper(live.aspect, size.width, size.height)
                drawFigure(live, m, Color.White, thickness = torsoPx(live, m) * 0.07f, alpha = 0.9f)
            }
            return@Canvas
        }

        if (target != null) {
            val ghost = if (live != null) PoseMath.alignTo(target, live) else target
            val m = fillCenterMapper(ghost.aspect, size.width, size.height)
            val thick = (torsoPx(ghost, m) * 0.22f).coerceIn(10f, 80f)
            drawFigure(
                ghost, m, ghostColor,
                thickness = thick,
                outline = Color.White,
                outlineWidth = thick * 0.18f,
                alpha = 0.55f
            )
        }
        if (live != null) {
            val m = fillCenterMapper(live.aspect, size.width, size.height)
            val thick = (torsoPx(live, m) * 0.06f).coerceIn(4f, 18f)
            drawFigure(
                live, m, Color.White,
                thickness = thick,
                boneColors = match?.boneScores?.map { boneColor(it) },
                jointColor = Color.White,
                jointRadius = thick * 0.9f
            )
        }
    }
}

@Composable
private fun TopBar(progress: Float, combo: Int, onClose: () -> Unit, onFlipCamera: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(SimpleIcon.CLOSE, Color.White, size = 26.dp)
        }
        Spacer(Modifier.width(8.dp))
        DuoProgressBar(
            progress = progress,
            modifier = Modifier.weight(1f),
            trackColor = Color.White.copy(alpha = 0.3f),
            height = 18.dp
        )
        AnimatedVisibility(visible = combo >= 2, enter = scaleIn(spring(dampingRatio = 0.4f)) + fadeIn(), exit = fadeOut()) {
            Row {
                Spacer(Modifier.width(10.dp))
                Pill(text = "$combo", color = Orange, leading = "🔥")
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onFlipCamera),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(SimpleIcon.CAMERA, Color.White, size = 24.dp)
        }
    }
}

@Composable
private fun TargetCard(pose: RecordedPose, color: Color, modifier: Modifier = Modifier) {
    DuoCard(modifier = modifier.size(width = 84.dp, height = 112.dp), fillWidth = true) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(6.dp)) {
            PoseThumbnail(pose, Modifier.size(width = 72.dp, height = 78.dp), color = color, padding = 0.08f)
            Text("CIBLE", style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textMuted)
        }
    }
}

@Composable
private fun SessionPanel(
    engine: WorkoutEngine,
    ui: WorkoutEngine.State,
    match: PoseMath.MatchResult?,
    rounded: RoundedCornerShape,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = AppTheme.colors
    val item = ui.item ?: return
    val step = item.step
    val success = ui.phase == Phase.SUCCESS
    val bg by animateColorAsState(
        if (success) (if (c.isDark) Color(0xFF1F3A12) else GreenLight) else c.surface,
        label = "panelBg"
    )

    Column(
        Modifier
            .fillMaxWidth()
            .clip(rounded)
            .background(bg)
            .then(modifier)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pill(step.type.instruction().uppercase(), step.type.color())
            Spacer(Modifier.weight(1f))
            val rounds = engine.exercise.safeRounds
            Text(
                text = if (rounds > 1) "Tour ${item.round + 1}/$rounds" else "Étape ${ui.index + 1}/${engine.plan.size}",
                style = MaterialTheme.typography.labelMedium,
                color = c.textMuted
            )
        }
        Spacer(Modifier.height(8.dp))

        if (success) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Green),
                    contentAlignment = Alignment.Center
                ) { AppIcon(SimpleIcon.CHECK, Color.White, size = 22.dp) }
                Spacer(Modifier.width(12.dp))
                Text(praise(ui.successEvents), style = MaterialTheme.typography.headlineSmall, color = GreenDark)
            }
            Spacer(Modifier.height(64.dp))
            return@Column
        }

        Text(
            step.displayName(item.stepIndex),
            style = MaterialTheme.typography.headlineSmall,
            color = c.text,
            maxLines = 1
        )
        Spacer(Modifier.height(12.dp))

        when {
            ui.phase == Phase.REP_BREAK -> {
                Text(
                    "Relâche ! Série ${ui.repsDone + 1}/${ui.repsTarget} dans ${ceil(ui.remainingMs / 1000f).toInt()} s",
                    style = MaterialTheme.typography.titleMedium,
                    color = Blue
                )
            }

            step.type == StepType.HOLD -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DuoProgressBar(
                        progress = ui.holdProgress,
                        modifier = Modifier.weight(1f),
                        color = if (ui.matched) Green else Blue,
                        height = 22.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "%.1f s".format(ui.remainingMs / 1000f).replace('.', ','),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (ui.matched) GreenDark else c.text
                    )
                }
                if (ui.repsTarget > 1) {
                    Spacer(Modifier.height(6.dp))
                    Text("Série ${ui.repsDone + 1}/${ui.repsTarget}", style = MaterialTheme.typography.labelMedium, color = c.textMuted)
                }
            }

            else -> {
                if (ui.repsTarget > 1) {
                    RepDots(ui.repsDone, ui.repsTarget)
                } else {
                    Text("Prends la pose pour valider !", style = MaterialTheme.typography.bodyLarge, color = c.textMuted)
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        SimilarityMeter(ui.score, hasPerson = match != null)

        val hint = when {
            match == null -> "On ne te voit pas, place-toi face à la caméra."
            !match.bodyVisible -> "Recule un peu pour qu'on te voie en entier."
            else -> null
        }
        if (hint != null) {
            Spacer(Modifier.height(8.dp))
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = Orange)
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Passer cette pose",
            style = MaterialTheme.typography.labelLarge,
            color = c.textMuted,
            modifier = Modifier
                .align(Alignment.End)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onSkip)
                .padding(8.dp)
        )
    }
}

@Composable
private fun RepDots(done: Int, total: Int) {
    val c = AppTheme.colors
    Column {
        Text("$done / $total", style = MaterialTheme.typography.titleLarge, color = c.text)
        Spacer(Modifier.height(6.dp))
        if (total <= 20) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(total) { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (i < done) Orange else c.border)
                    )
                }
            }
        } else {
            DuoProgressBar(done / total.toFloat(), Modifier.fillMaxWidth(), color = Orange)
        }
    }
}

@Composable
private fun SimilarityMeter(score: Float, hasPerson: Boolean) {
    val c = AppTheme.colors
    val color = when {
        !hasPerson -> c.border
        score >= 0.75f -> Green
        score >= 0.5f -> Yellow
        else -> Red
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Ressemblance", style = MaterialTheme.typography.labelMedium, color = c.textMuted)
        Spacer(Modifier.width(10.dp))
        DuoProgressBar(
            progress = if (hasPerson) score else 0f,
            modifier = Modifier.weight(1f),
            color = color,
            height = 10.dp
        )
        Spacer(Modifier.width(10.dp))
        Text(
            if (hasPerson) "${(score * 100).toInt()} %" else "—",
            style = MaterialTheme.typography.labelLarge,
            color = c.text
        )
    }
}

@Composable
private fun GetReadyOverlay(ui: WorkoutEngine.State, frame: LiveFrame?, onSkip: () -> Unit) {
    val seconds = ceil(ui.remainingMs / 1000f).toInt().coerceAtLeast(1)
    val pop = remember { Animatable(1f) }
    LaunchedEffect(seconds) {
        pop.snapTo(1.4f)
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
    }
    val visible = frame?.pose != null
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSkip
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("Prépare-toi !", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .size(140.dp)
                    .scale(pop.value)
                    .clip(CircleShape)
                    .background(Green),
                contentAlignment = Alignment.Center
            ) {
                Text("$seconds", style = MaterialTheme.typography.displayLarge, color = Color.White)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                if (visible) "On te voit 👍" else "Place-toi pour qu'on te voie en entier",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text("Touche l'écran pour commencer tout de suite", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun PauseOverlay(engine: WorkoutEngine, ui: WorkoutEngine.State, onSkip: () -> Unit) {
    val step = ui.item?.step ?: return
    val next = engine.plan.getOrNull(ui.index + 1)
    val remaining = ceil(ui.remainingMs / 1000f).toInt()
    Box(
        Modifier
            .fillMaxSize()
            .background(Purple.copy(alpha = 0.94f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSkip
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(28.dp)
                .widthIn(max = 420.dp)
        ) {
            Text("☕ Pause", style = MaterialTheme.typography.headlineLarge, color = Color.White)
            Spacer(Modifier.height(20.dp))
            Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val sw = 16.dp.toPx()
                    drawArc(
                        Color.White.copy(alpha = 0.25f), 0f, 360f, false,
                        Offset(sw / 2, sw / 2), Size(size.width - sw, size.height - sw),
                        style = Stroke(sw)
                    )
                    drawArc(
                        Color.White, -90f, 360f * (1f - ui.holdProgress), false,
                        Offset(sw / 2, sw / 2), Size(size.width - sw, size.height - sw),
                        style = Stroke(sw, cap = StrokeCap.Round)
                    )
                }
                Text("$remaining", style = MaterialTheme.typography.displayLarge, color = Color.White)
            }
            Spacer(Modifier.height(24.dp))
            if (next != null && next.step.pose != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(12.dp)
                ) {
                    PoseThumbnail(next.step.pose, Modifier.size(56.dp), color = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("ENSUITE", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                        Text(next.step.displayName(next.stepIndex), style = MaterialTheme.typography.titleLarge, color = Color.White)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
            DuoButton(
                text = "Passer la pause",
                onClick = onSkip,
                faceColor = Color.White,
                shadowColor = Color.White.copy(alpha = 0.6f),
                contentColor = step.type.shadow()
            )
            Spacer(Modifier.height(8.dp))
            Text("ou touche n'importe où", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun QuitDialog(onDismiss: () -> Unit, onQuit: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surface,
        title = { Text("Tu abandonnes déjà ?", style = MaterialTheme.typography.headlineSmall, color = AppTheme.colors.text) },
        text = { Text("Ta progression sur cette séance sera perdue.", style = MaterialTheme.typography.bodyLarge, color = AppTheme.colors.textMuted) },
        confirmButton = {
            Column(Modifier.fillMaxWidth()) {
                DuoButton("Continuer", onClick = onDismiss)
                Spacer(Modifier.height(8.dp))
                DuoButton("Quitter", onClick = onQuit, style = DuoStyle.GHOST, contentColor = Red, faceColor = Color.Transparent)
            }
        }
    )
}

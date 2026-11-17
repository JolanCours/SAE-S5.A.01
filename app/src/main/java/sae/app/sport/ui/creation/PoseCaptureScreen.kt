package sae.app.sport.ui.creation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sae.app.sport.data.BuiltInExercises
import sae.app.sport.model.RecordedPose
import sae.app.sport.pose.PoseMath
import sae.app.sport.ui.camera.CameraPermissionGate
import sae.app.sport.ui.camera.LiveFrame
import sae.app.sport.ui.camera.PoseCameraPreview
import sae.app.sport.ui.components.AppIcon
import sae.app.sport.ui.components.DuoButton
import sae.app.sport.ui.components.DuoPressable
import sae.app.sport.ui.components.PoseThumbnail
import sae.app.sport.ui.components.SimpleIcon
import sae.app.sport.ui.components.drawFigure
import sae.app.sport.ui.components.fillCenterMapper
import sae.app.sport.ui.theme.Green
import sae.app.sport.ui.theme.GreenDark
import sae.app.sport.ui.theme.Orange
import sae.app.sport.ui.theme.Red
import sae.app.sport.ui.theme.RedDark
import java.util.concurrent.atomic.AtomicReference

@Composable
fun PoseCaptureScreen(
    single: Boolean,
    onDone: (List<RecordedPose>) -> Unit,
    onCancel: () -> Unit
) {
    BackHandler(onBack = onCancel)
    CameraPermissionGate {
        CaptureContent(single, onDone, onCancel)
    }
}

private fun isBodyVisible(p: RecordedPose?): Boolean =
    p != null && PoseMath.isValid(p) &&
        PoseMath.KEY_JOINTS.count { (p.landmarks[it].visibility ?: 1f) > 0.5f } >= 11

@Composable
private fun CaptureContent(
    single: Boolean,
    onDone: (List<RecordedPose>) -> Unit,
    onCancel: () -> Unit
) {
    val latest = remember { AtomicReference<LiveFrame?>(null) }
    var frame by remember { mutableStateOf<LiveFrame?>(null) }
    var frontCamera by rememberSaveable { mutableStateOf(true) }
    var timerSec by rememberSaveable { mutableStateOf(5) }
    var countdown by remember { mutableStateOf<Int?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var modelError by remember { mutableStateOf<String?>(null) }
    val captured = remember { mutableStateListOf<RecordedPose>() }
    val flash = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { }
            frame = latest.get()?.takeIf { it.isFresh() }
        }
    }

    fun takeNow() {
        val pose = latest.get()?.takeIf { it.isFresh() }?.pose
        val result = if (pose != null && PoseMath.isValid(pose)) {
            message = if (isBodyVisible(pose)) null else "Pose capturée, mais une partie du corps n'était pas visible : retouche-la dans l'éditeur."
            pose
        } else {
            message = "Personne détecté : une pose par défaut a été ajoutée, tu pourras la modifier au doigt."
            BuiltInExercises.standingPose
        }
        scope.launch {
            flash.snapTo(1f)
            flash.animateTo(0f, tween(350))
        }
        if (single) onDone(listOf(result)) else captured.add(result)
    }

    LaunchedEffect(countdown) {
        val c = countdown ?: return@LaunchedEffect
        if (c <= 0) {
            countdown = null
            takeNow()
        } else {
            delay(1000)
            countdown = c - 1
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        PoseCameraPreview(
            modifier = Modifier.fillMaxSize(),
            frontCamera = frontCamera,
            onFrame = { latest.set(it) },
            onModelError = { modelError = it }
        )

        val live = frame?.pose
        val visible = isBodyVisible(live)
        Canvas(Modifier.fillMaxSize()) {
            if (live != null && PoseMath.isValid(live)) {
                val m = fillCenterMapper(live.aspect, size.width, size.height)
                drawFigure(
                    live, m, if (visible) Green else Orange,
                    thickness = 7.dp.toPx(),
                    outline = Color.White,
                    outlineWidth = 2.dp.toPx(),
                    alpha = 0.8f,
                    jointColor = Color.White,
                    jointRadius = 5.dp.toPx()
                )
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)))
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIcon(SimpleIcon.CLOSE, onCancel)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (single) "Reprendre la pose" else "Enregistrer des poses",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        when {
                            live == null -> "Place-toi face à la caméra"
                            !visible -> "Recule pour qu'on te voie en entier"
                            else -> "C'est bon, on te voit en entier ✓"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (visible) Color.White else Color(0xFFFFD08A)
                    )
                }
                RoundIcon(SimpleIcon.CAMERA) { frontCamera = !frontCamera }
            }
            message?.let {
                Spacer(Modifier.height(10.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Orange.copy(alpha = 0.9f))
                        .clickable { message = null }
                        .padding(10.dp)
                )
            }
            modelError?.let {
                Spacer(Modifier.height(10.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Red)
                        .padding(10.dp)
                )
            }
        }

        countdown?.let { c ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (c > 0) "$c" else "📸", style = MaterialTheme.typography.displayLarge, color = Color.White)
                }
            }
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (captured.isNotEmpty()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    captured.forEachIndexed { i, p ->
                        Box(
                            Modifier
                                .size(width = 54.dp, height = 70.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .clickable { captured.removeAt(i) }
                        ) {
                            PoseThumbnail(p, Modifier.fillMaxSize(), color = Green)
                        }
                    }
                }
                Text(
                    "Touche une vignette pour la retirer",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(12.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 3, 5, 10).forEach { s ->
                    val sel = s == timerSec
                    Text(
                        if (s == 0) "Direct" else "$s s",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (sel) Color.Black else Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (sel) Color.White else Color.White.copy(alpha = 0.2f))
                            .clickable(enabled = countdown == null) { timerSec = s }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f))
                DuoPressable(
                    onClick = {
                        if (countdown != null) countdown = null
                        else if (timerSec == 0) takeNow()
                        else countdown = timerSec
                    },
                    modifier = Modifier.size(width = 84.dp, height = 90.dp),
                    face = if (countdown != null) Color.White else Red,
                    shadow = if (countdown != null) Color.LightGray else RedDark,
                    shape = CircleShape,
                    minHeight = 84.dp,
                    depth = 6.dp
                ) {
                    if (countdown != null) {
                        AppIcon(SimpleIcon.CLOSE, Red, size = 32.dp)
                    } else {
                        Box(
                            Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (!single && captured.isNotEmpty()) {
                        DuoButton(
                            "OK (${captured.size})",
                            onClick = { onDone(captured.toList()) },
                            fillWidth = false,
                            faceColor = Green,
                            shadowColor = GreenDark,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                if (countdown != null) "Touche pour annuler" else "Le retardateur te laisse le temps de te placer",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        if (flash.value > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flash.value * 0.8f))
            )
        }
    }
}

@Composable
private fun RoundIcon(icon: SimpleIcon, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(icon, Color.White, size = 24.dp)
    }
}

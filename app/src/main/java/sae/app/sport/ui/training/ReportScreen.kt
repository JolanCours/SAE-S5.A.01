package sae.app.sport.ui.training

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import sae.app.sport.model.PoseExercise
import sae.app.sport.model.StepType
import sae.app.sport.training.WorkoutEngine
import sae.app.sport.ui.components.DuoButton
import sae.app.sport.ui.components.DuoCard
import sae.app.sport.ui.components.DuoProgressBar
import sae.app.sport.ui.components.DuoStyle
import sae.app.sport.ui.components.PoseThumbnail
import sae.app.sport.ui.components.SectionHeader
import sae.app.sport.ui.components.color
import sae.app.sport.ui.theme.AppTheme
import sae.app.sport.ui.theme.Blue
import sae.app.sport.ui.theme.Green
import sae.app.sport.ui.theme.Orange
import sae.app.sport.ui.theme.Pink
import sae.app.sport.ui.theme.Purple
import sae.app.sport.ui.theme.Red
import sae.app.sport.ui.theme.Yellow
import sae.app.sport.ui.theme.YellowDark
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ReportScreen(
    exercise: PoseExercise,
    report: WorkoutEngine.Report,
    onContinue: () -> Unit,
    onRetry: () -> Unit
) {
    val c = AppTheme.colors
    BackHandler(onBack = onContinue)

    Box(
        Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 600.dp
            Column(
                Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    Modifier.widthIn(max = if (wide) 760.dp else 520.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))
                    StarsRow(report.stars)
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = when (report.stars) {
                            3 -> "Séance parfaite !"
                            2 -> "Bien joué !"
                            1 -> "Séance terminée !"
                            else -> "Tu y es presque !"
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (report.stars >= 2) YellowDark else c.text,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = when (report.stars) {
                            3 -> "Toutes les poses validées avec une super précision."
                            2 -> "Encore un petit effort pour les 3 étoiles."
                            1 -> "Chaque séance compte. Recommence pour progresser !"
                            else -> "Essaie de bien te placer face à la caméra."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = c.textMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(28.dp))

                    val stats = listOf(
                        StatData("Précision", "${(report.accuracy * 100).toInt()} %", "🎯", Green),
                        StatData("Temps", formatClock(report.totalMs), "⏱️", Blue),
                        StatData("Combo max", "${report.maxCombo}", "🔥", Orange),
                        StatData("Réussies", "${report.successCount}/${report.poseCount}", "✅", Purple)
                    )
                    if (wide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            stats.forEachIndexed { i, s -> StatCard(s, i, Modifier.weight(1f)) }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            stats.chunked(2).forEachIndexed { row, pair ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    pair.forEachIndexed { i, s -> StatCard(s, row * 2 + i, Modifier.weight(1f)) }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))
                    StepBreakdown(exercise, report)
                    Spacer(Modifier.height(28.dp))

                    DuoButton("Continuer", onClick = onContinue)
                    Spacer(Modifier.height(10.dp))
                    DuoButton("Recommencer", onClick = onRetry, style = DuoStyle.OUTLINE)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
        if (report.stars >= 2) Confetti(Modifier.fillMaxSize())
    }
}

private fun formatClock(ms: Long): String {
    val s = (ms / 1000).toInt()
    return "%d:%02d".format(s / 60, s % 60)
}

private data class StatData(val label: String, val value: String, val emoji: String, val color: Color)

@Composable
private fun StatCard(data: StatData, index: Int, modifier: Modifier = Modifier) {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(500L + index * 140L)
        appear.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    Box(
        modifier
            .scale(0.6f + 0.4f * appear.value)
            .clip(RoundedCornerShape(18.dp))
            .background(data.color)
            .padding(2.dp)
    ) {
        Column {
            Text(
                data.label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                textAlign = TextAlign.Center
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surface)
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(data.emoji, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(6.dp))
                Text(data.value, style = MaterialTheme.typography.titleLarge, color = data.color)
            }
        }
    }
}

@Composable
private fun StarsRow(stars: Int) {
    val empty = AppTheme.colors.border
    val emptyShadow = if (AppTheme.colors.isDark) Color(0xFF2A3940) else Color(0xFFCECECE)
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        for (i in 0 until 3) {
            val anim = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                delay(200L + i * 250L)
                anim.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow))
            }
            val big = i == 1
            Canvas(
                Modifier
                    .size(if (big) 88.dp else 68.dp)
                    .padding(bottom = if (big) 12.dp else 0.dp)
                    .scale(anim.value)
            ) {
                val filled = i < stars
                drawStar(if (filled) Yellow else empty, if (filled) YellowDark else emptyShadow)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStar(fill: Color, shadow: Color) {
    fun starPath(dy: Float): Path {
        val cx = size.width / 2
        val cy = size.height / 2 + dy
        val outer = size.minDimension / 2 * 0.95f
        val inner = outer * 0.5f
        return Path().apply {
            for (k in 0 until 10) {
                val r = if (k % 2 == 0) outer else inner
                val a = -PI / 2 + k * PI / 5
                val x = cx + (r * cos(a)).toFloat()
                val y = cy + (r * sin(a)).toFloat()
                if (k == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
    }
    drawPath(starPath(size.height * 0.04f), shadow)
    drawPath(starPath(0f), fill)
}

@Composable
private fun StepBreakdown(exercise: PoseExercise, report: WorkoutEngine.Report) {
    val c = AppTheme.colors
    val groups = report.results.groupBy { it.stepIndex }.toSortedMap()
    if (groups.isEmpty()) return
    Column(Modifier.fillMaxWidth()) {
        SectionHeader("Détail des poses")
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for ((stepIndex, results) in groups) {
                val step = exercise.steps.getOrNull(stepIndex) ?: continue
                val ok = results.count { it.success }
                val successes = results.filter { it.success }
                val quality = if (successes.isEmpty()) 0f else successes.map { it.quality }.average().toFloat()
                DuoCard {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(step.type.color().copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            PoseThumbnail(step.pose, Modifier.fillMaxSize(), color = step.type.color())
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(step.displayName(stepIndex), style = MaterialTheme.typography.titleMedium, color = c.text, maxLines = 1)
                            Text(
                                if (step.type == StepType.INSTANT) "$ok/${results.size} validées · ${results.sumOf { it.repsDone }} reps"
                                else "$ok/${results.size} validées",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textMuted
                            )
                            Spacer(Modifier.height(6.dp))
                            DuoProgressBar(
                                progress = quality,
                                modifier = Modifier.fillMaxWidth(),
                                color = when {
                                    quality >= 0.85f -> Green
                                    quality >= 0.7f -> Yellow
                                    else -> Red
                                },
                                height = 10.dp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("${(quality * 100).toInt()} %", style = MaterialTheme.typography.titleMedium, color = c.text)
                    }
                }
            }
        }
    }
}

@Composable
private fun Confetti(modifier: Modifier = Modifier) {
    val colors = listOf(Green, Blue, Orange, Yellow, Purple, Pink, Red)
    val pieces = remember {
        val rnd = Random(7)
        List(90) {
            floatArrayOf(
                rnd.nextFloat(),
                -rnd.nextFloat() * 0.6f,
                0.25f + rnd.nextFloat() * 0.35f,
                rnd.nextFloat() * 360f,
                (rnd.nextFloat() - 0.5f) * 400f,
                rnd.nextInt(colors.size).toFloat(),
                rnd.nextFloat() * 6.28f
            )
        }
    }
    var t by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        while (t < 4.5f) {
            t = (withFrameMillis { it } - start) / 1000f
        }
    }
    if (t >= 4.5f) return
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val pw = 10.dp.toPx()
        val ph = 16.dp.toPx()
        for (p in pieces) {
            val y = (p[1] + p[2] * t) * h
            if (y > h + ph) continue
            val x = p[0] * w + sin(p[6] + t * 3f) * 20.dp.toPx()
            rotate(p[3] + p[4] * t, Offset(x, y)) {
                drawRect(
                    colors[p[5].toInt()],
                    topLeft = Offset(x - pw / 2, y - ph / 2),
                    size = Size(pw, ph),
                    alpha = (1f - (t - 3.5f).coerceAtLeast(0f)).coerceIn(0f, 1f)
                )
            }
        }
    }
}

package sae.app.sport.ui.creation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import sae.app.sport.model.RecordedPose
import sae.app.sport.pose.PoseMath
import sae.app.sport.ui.components.drawFigure
import sae.app.sport.ui.components.fitCenterInverse
import sae.app.sport.ui.components.fitCenterMapper
import sae.app.sport.ui.theme.AppTheme
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun PoseEditorCanvas(
    pose: RecordedPose,
    color: Color,
    modifier: Modifier = Modifier,
    onDragStart: () -> Unit,
    onPoseChange: (RecordedPose) -> Unit
) {
    val c = AppTheme.colors
    val currentPose by rememberUpdatedState(pose)
    val currentOnChange by rememberUpdatedState(onPoseChange)
    val currentOnStart by rememberUpdatedState(onDragStart)
    var activeJoint by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { start ->
                    val p = currentPose
                    if (!PoseMath.isValid(p)) return@detectDragGestures
                    val mapper = fitCenterMapper(p.aspect, size.width.toFloat(), size.height.toFloat())
                    val maxDist = 44.dp.toPx()
                    var best: Int? = null
                    var bestD = Float.MAX_VALUE
                    for (j in PoseMath.KEY_JOINTS) {
                        val o = mapper.map(p.landmarks[j].x, p.landmarks[j].y)
                        val dx = o.x - start.x
                        val dy = o.y - start.y
                        val d = sqrt(dx * dx + dy * dy)
                        if (d < bestD) {
                            bestD = d
                            best = j
                        }
                    }
                    if (best != null && bestD <= maxDist) {
                        activeJoint = best
                        currentOnStart()
                    }
                },
                onDrag = { change, _ ->
                    val j = activeJoint ?: return@detectDragGestures
                    change.consume()
                    val p = currentPose
                    val (nx, ny) = fitCenterInverse(p.aspect, size.width.toFloat(), size.height.toFloat(), change.position)
                    currentOnChange(PoseMath.moveJoint(p, j, nx, ny))
                },
                onDragEnd = { activeJoint = null },
                onDragCancel = { activeJoint = null }
            )
        }
    ) {
        if (!PoseMath.isValid(pose)) return@Canvas
        val w = size.width
        val h = size.height
        val mapper = fitCenterMapper(pose.aspect, w, h)

        val tl = mapper.map(0f, 0f)
        val br = mapper.map(1f, 1f)
        drawRoundRect(
            color = c.surfaceAlt,
            topLeft = tl,
            size = Size(br.x - tl.x, br.y - tl.y),
            cornerRadius = CornerRadius(16.dp.toPx())
        )
        val grid = c.border.copy(alpha = 0.7f)
        for (k in 1..3) {
            val x = tl.x + (br.x - tl.x) * k / 4f
            val y = tl.y + (br.y - tl.y) * k / 4f
            drawLine(grid, Offset(x, tl.y), Offset(x, br.y), 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
            drawLine(grid, Offset(tl.x, y), Offset(br.x, y), 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
        }

        val l = pose.landmarks
        val sh = mapper.map((l[11].x + l[12].x) / 2f, (l[11].y + l[12].y) / 2f)
        val hip = mapper.map((l[23].x + l[24].x) / 2f, (l[23].y + l[24].y) / 2f)
        val torso = sqrt((sh.x - hip.x) * (sh.x - hip.x) + (sh.y - hip.y) * (sh.y - hip.y))
        val thick = (torso * 0.16f).coerceIn(6.dp.toPx(), min(w, h) * 0.05f)
        drawFigure(pose, mapper, color, thickness = thick, alpha = 0.85f)

        val r = 11.dp.toPx()
        for (j in PoseMath.KEY_JOINTS) {
            val o = mapper.map(l[j].x, l[j].y)
            val active = j == activeJoint
            drawCircle(if (active) color else Color.White, if (active) r * 1.35f else r, o)
            drawCircle(color, if (active) r * 1.35f else r, o, style = Stroke(3.dp.toPx()))
        }
    }
}

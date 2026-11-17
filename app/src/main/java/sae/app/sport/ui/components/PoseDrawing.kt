package sae.app.sport.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import sae.app.sport.model.RecordedPose
import sae.app.sport.pose.PoseMath
import sae.app.sport.ui.theme.Green
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

fun interface PoseMapper {
    fun map(x: Float, y: Float): Offset
}

fun fillCenterMapper(aspect: Float, viewW: Float, viewH: Float): PoseMapper {
    val scale = max(viewW / aspect, viewH)
    val offX = (viewW - aspect * scale) / 2f
    val offY = (viewH - scale) / 2f
    return PoseMapper { x, y -> Offset(x * aspect * scale + offX, y * scale + offY) }
}

fun fitCenterMapper(aspect: Float, viewW: Float, viewH: Float): PoseMapper {
    val scale = min(viewW / aspect, viewH)
    val offX = (viewW - aspect * scale) / 2f
    val offY = (viewH - scale) / 2f
    return PoseMapper { x, y -> Offset(x * aspect * scale + offX, y * scale + offY) }
}

fun fitCenterInverse(aspect: Float, viewW: Float, viewH: Float, p: Offset): Pair<Float, Float> {
    val scale = min(viewW / aspect, viewH)
    val offX = (viewW - aspect * scale) / 2f
    val offY = (viewH - scale) / 2f
    return ((p.x - offX) / (aspect * scale)) to ((p.y - offY) / scale)
}

fun fitBodyMapper(pose: RecordedPose, viewW: Float, viewH: Float, padding: Float = 0.14f): PoseMapper {
    val b = PoseMath.bounds(pose)
    val headExtra = (b[3] - b[1]) * 0.08f
    val minX = b[0]; val maxX = b[2]
    val minY = b[1] - headExtra; val maxY = b[3]
    val bw = (maxX - minX).coerceAtLeast(1e-3f)
    val bh = (maxY - minY).coerceAtLeast(1e-3f)
    val availW = viewW * (1 - 2 * padding)
    val availH = viewH * (1 - 2 * padding)
    val scale = min(availW / bw, availH / bh)
    val offX = (viewW - bw * scale) / 2f - minX * scale
    val offY = (viewH - bh * scale) / 2f - minY * scale
    return PoseMapper { x, y -> Offset(x * pose.aspect * scale + offX, y * scale + offY) }
}

private fun dist(a: Offset, b: Offset): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
}

fun DrawScope.drawFigure(
    pose: RecordedPose,
    mapper: PoseMapper,
    color: Color,
    thickness: Float,
    outline: Color? = null,
    outlineWidth: Float = thickness * 0.35f,
    boneColors: List<Color?>? = null,
    alpha: Float = 1f,
    jointColor: Color? = null,
    jointRadius: Float = 0f,
    headColor: Color = color
) {
    if (!PoseMath.isValid(pose)) return
    val p = pose.landmarks.map { mapper.map(it.x, it.y) }

    val lSh = p[PoseMath.L_SHOULDER]; val rSh = p[PoseMath.R_SHOULDER]
    val lHip = p[PoseMath.L_HIP]; val rHip = p[PoseMath.R_HIP]
    val midSh = Offset((lSh.x + rSh.x) / 2f, (lSh.y + rSh.y) / 2f)
    val midHip = Offset((lHip.x + rHip.x) / 2f, (lHip.y + rHip.y) / 2f)
    val torsoLen = dist(midSh, midHip)
    val headR = max(max(dist(lSh, rSh) * 0.30f, torsoLen * 0.20f), thickness * 1.1f)
    val nose = p[PoseMath.NOSE]
    val earMid = Offset((p[7].x + p[8].x) / 2f, (p[7].y + p[8].y) / 2f)
    val headC = Offset((nose.x + earMid.x) / 2f, (nose.y + earMid.y) / 2f - headR * 0.15f)

    val torso = Path().apply {
        moveTo(lSh.x, lSh.y); lineTo(rSh.x, rSh.y); lineTo(rHip.x, rHip.y); lineTo(lHip.x, lHip.y); close()
    }
    val limbs = PoseMath.BONES.withIndex().filter { (_, b) ->
        !(b == PoseMath.L_SHOULDER to PoseMath.R_SHOULDER || b == PoseMath.L_HIP to PoseMath.R_HIP)
    }
    val feet = listOf(PoseMath.L_ANKLE to 31, PoseMath.R_ANKLE to 32)

    fun pass(width: Float, c: Color?, perBone: Boolean) {
        val baseColor = (c ?: color).copy(alpha = (c ?: color).alpha * alpha)
        drawLine(baseColor, midSh, headC, width * 0.9f, StrokeCap.Round)
        drawPath(torso, baseColor, style = Fill)
        drawPath(torso, baseColor, style = Stroke(width = width, join = StrokeJoin.Round))
        for ((i, b) in limbs) {
            val bc = if (perBone) boneColors?.getOrNull(i) ?: baseColor else baseColor
            drawLine(bc.copy(alpha = bc.alpha * if (perBone) alpha else 1f), p[b.first], p[b.second], width, StrokeCap.Round)
        }
        for ((a, b) in feet) drawLine(baseColor, p[a], p[b], width * 0.75f, StrokeCap.Round)
        val hc = if (c == null) headColor.copy(alpha = headColor.alpha * alpha) else baseColor
        drawCircle(hc, headR + (width - thickness) / 2f, headC)
    }

    if (outline != null) pass(thickness + outlineWidth * 2, outline.copy(alpha = outline.alpha * alpha), perBone = false)
    pass(thickness, null, perBone = boneColors != null)

    if (jointColor != null && jointRadius > 0f) {
        for (j in PoseMath.KEY_JOINTS) {
            if (j == PoseMath.NOSE) continue
            drawCircle(jointColor.copy(alpha = alpha), jointRadius, p[j])
        }
    }
}

@Composable
fun PoseThumbnail(
    pose: RecordedPose?,
    modifier: Modifier = Modifier,
    color: Color = Green,
    thicknessFraction: Float = 0.055f,
    padding: Float = 0.12f
) {
    Canvas(modifier) {
        if (pose == null || !PoseMath.isValid(pose)) return@Canvas
        val mapper = fitBodyMapper(pose, size.width, size.height, padding)
        drawFigure(pose, mapper, color, thickness = min(size.width, size.height) * thicknessFraction)
    }
}

package sae.app.sport.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class SimpleIcon { CLOSE, BACK, PLUS, CHECK, PLAY, PAUSE, CHEVRON_LEFT, CHEVRON_RIGHT, TRASH, COPY, CAMERA, MIRROR, EDIT, TIMER, RESET }

@Composable
fun AppIcon(icon: SimpleIcon, color: Color, modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Canvas(modifier.size(size)) { drawSimpleIcon(icon, color) }
}

fun DrawScope.drawSimpleIcon(icon: SimpleIcon, color: Color) {
    val w = size.width
    val h = size.height
    val sw = w * 0.12f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, width: Float = sw) =
        drawLine(color, Offset(x1 * w, y1 * h), Offset(x2 * w, y2 * h), width, StrokeCap.Round)

    fun path(vararg pts: Pair<Float, Float>, close: Boolean = false, fill: Boolean = false) {
        val p = Path()
        pts.forEachIndexed { i, (x, y) -> if (i == 0) p.moveTo(x * w, y * h) else p.lineTo(x * w, y * h) }
        if (close) p.close()
        if (fill) drawPath(p, color) else drawPath(p, color, style = stroke)
    }

    when (icon) {
        SimpleIcon.CLOSE -> { line(0.22f, 0.22f, 0.78f, 0.78f); line(0.78f, 0.22f, 0.22f, 0.78f) }
        SimpleIcon.BACK -> { line(0.2f, 0.5f, 0.82f, 0.5f); path(0.48f to 0.2f, 0.18f to 0.5f, 0.48f to 0.8f) }
        SimpleIcon.PLUS -> { line(0.5f, 0.18f, 0.5f, 0.82f); line(0.18f, 0.5f, 0.82f, 0.5f) }
        SimpleIcon.CHECK -> path(0.18f to 0.52f, 0.42f to 0.76f, 0.84f to 0.26f)
        SimpleIcon.PLAY -> path(0.3f to 0.18f, 0.84f to 0.5f, 0.3f to 0.82f, close = true, fill = true)
        SimpleIcon.PAUSE -> { line(0.34f, 0.2f, 0.34f, 0.8f, sw * 1.6f); line(0.66f, 0.2f, 0.66f, 0.8f, sw * 1.6f) }
        SimpleIcon.CHEVRON_LEFT -> path(0.62f to 0.18f, 0.3f to 0.5f, 0.62f to 0.82f)
        SimpleIcon.CHEVRON_RIGHT -> path(0.38f to 0.18f, 0.7f to 0.5f, 0.38f to 0.82f)
        SimpleIcon.TRASH -> {
            line(0.16f, 0.26f, 0.84f, 0.26f)
            path(0.38f to 0.26f, 0.4f to 0.14f, 0.6f to 0.14f, 0.62f to 0.26f)
            path(0.24f to 0.26f, 0.3f to 0.86f, 0.7f to 0.86f, 0.76f to 0.26f)
            line(0.43f, 0.42f, 0.44f, 0.7f); line(0.57f, 0.42f, 0.56f, 0.7f)
        }
        SimpleIcon.COPY -> {
            drawRoundRect(color, Offset(0.34f * w, 0.34f * h), Size(0.5f * w, 0.5f * h),
                androidx.compose.ui.geometry.CornerRadius(0.1f * w), style = stroke)
            path(0.2f to 0.62f, 0.16f to 0.62f, 0.16f to 0.16f, 0.62f to 0.16f, 0.62f to 0.2f)
        }
        SimpleIcon.CAMERA -> {
            drawRoundRect(color, Offset(0.1f * w, 0.3f * h), Size(0.8f * w, 0.54f * h),
                androidx.compose.ui.geometry.CornerRadius(0.12f * w), style = stroke)
            path(0.34f to 0.3f, 0.4f to 0.18f, 0.6f to 0.18f, 0.66f to 0.3f)
            drawCircle(color, 0.14f * w, Offset(0.5f * w, 0.57f * h), style = stroke)
        }
        SimpleIcon.MIRROR -> {
            line(0.5f, 0.12f, 0.5f, 0.88f, sw * 0.7f)
            path(0.38f to 0.24f, 0.12f to 0.76f, 0.38f to 0.76f, close = true)
            path(0.62f to 0.24f, 0.88f to 0.76f, 0.62f to 0.76f, close = true, fill = true)
        }
        SimpleIcon.EDIT -> {
            path(0.2f to 0.8f, 0.24f to 0.62f, 0.66f to 0.2f, 0.8f to 0.34f, 0.38f to 0.76f, close = true)
        }
        SimpleIcon.TIMER -> {
            drawCircle(color, 0.34f * w, Offset(0.5f * w, 0.56f * h), style = stroke)
            line(0.5f, 0.56f, 0.5f, 0.38f); line(0.4f, 0.1f, 0.6f, 0.1f)
        }
        SimpleIcon.RESET -> {
            drawArc(color, 60f, 270f, false, Offset(0.18f * w, 0.18f * h), Size(0.64f * w, 0.64f * h), style = stroke)
            path(0.58f to 0.08f, 0.78f to 0.2f, 0.6f to 0.34f)
        }
    }
}

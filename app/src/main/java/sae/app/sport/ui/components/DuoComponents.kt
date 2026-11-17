package sae.app.sport.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import sae.app.sport.ui.theme.AppTheme
import sae.app.sport.ui.theme.Blue
import sae.app.sport.ui.theme.BlueDark
import sae.app.sport.ui.theme.Green
import sae.app.sport.ui.theme.GreenDark
import sae.app.sport.ui.theme.Red
import sae.app.sport.ui.theme.RedDark

enum class DuoStyle { PRIMARY, SECONDARY, DANGER, OUTLINE, GHOST }

@Composable
fun DuoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: DuoStyle = DuoStyle.PRIMARY,
    enabled: Boolean = true,
    fillWidth: Boolean = true,
    height: Dp = 52.dp,
    faceColor: Color? = null,
    shadowColor: Color? = null,
    contentColor: Color? = null,
    leading: (@Composable () -> Unit)? = null
) {
    val c = AppTheme.colors
    val (face, shadow, content) = when {
        !enabled -> Triple(c.disabled, c.disabled, c.textMuted)
        faceColor != null -> Triple(faceColor, shadowColor ?: faceColor, contentColor ?: Color.White)
        else -> when (style) {
            DuoStyle.PRIMARY -> Triple(Green, GreenDark, Color.White)
            DuoStyle.SECONDARY -> Triple(Blue, BlueDark, Color.White)
            DuoStyle.DANGER -> Triple(Red, RedDark, Color.White)
            DuoStyle.OUTLINE -> Triple(c.surface, c.border, Blue)
            DuoStyle.GHOST -> Triple(Color.Transparent, Color.Transparent, Blue)
        }
    }
    DuoPressable(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        face = face,
        shadow = shadow,
        border = if (style == DuoStyle.OUTLINE && enabled && faceColor == null) c.border else null,
        fillWidth = fillWidth,
        minHeight = height,
        depth = if (style == DuoStyle.GHOST) 0.dp else 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 18.dp)
        ) {
            if (leading != null) {
                leading()
                Box(Modifier.size(8.dp))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DuoPressable(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    face: Color,
    shadow: Color,
    border: Color? = null,
    shape: Shape = RoundedCornerShape(16.dp),
    fillWidth: Boolean = true,
    minHeight: Dp = 0.dp,
    depth: Dp = 4.dp,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateDpAsState(
        targetValue = if (pressed && enabled && onClick != null) depth else 0.dp,
        animationSpec = tween(70),
        label = "press"
    )
    val clickable = if (onClick != null) {
        Modifier.clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
    } else Modifier

    Box(modifier.then(clickable)) {
        Box(
            Modifier
                .matchParentSize()
                .padding(top = depth)
                .clip(shape)
                .background(shadow)
        )
        Box(
            modifier = Modifier
                .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                .padding(bottom = depth)
                .offset { IntOffset(0, press.roundToPx()) }
                .clip(shape)
                .background(face)
                .then(if (border != null) Modifier.border(2.dp, border, shape) else Modifier)
                .heightIn(min = minHeight),
            contentAlignment = contentAlignment,
            content = content
        )
    }
}

@Composable
fun DuoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    accent: Color = Blue,
    fillWidth: Boolean = true,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    val c = AppTheme.colors
    DuoPressable(
        onClick = onClick,
        modifier = modifier,
        face = if (selected) accent.copy(alpha = if (c.isDark) 0.18f else 0.10f).compositeOver(c.surface) else c.surface,
        shadow = if (selected) accent else c.border,
        border = if (selected) accent else c.border,
        fillWidth = fillWidth,
        depth = 4.dp,
        contentAlignment = contentAlignment,
        content = content
    )
}

private fun Color.compositeOver(bg: Color): Color {
    val a = alpha
    return Color(
        red = red * a + bg.red * (1 - a),
        green = green * a + bg.green * (1 - a),
        blue = blue * a + bg.blue * (1 - a),
        alpha = 1f
    )
}

@Composable
fun DuoProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Green,
    trackColor: Color = AppTheme.colors.border,
    height: Dp = 16.dp
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "progress"
    )
    Box(
        modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
    ) {
        if (animated > 0.001f) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated.coerceAtLeast(0.04f))
                    .clip(RoundedCornerShape(50))
                    .background(color)
            ) {
                Box(
                    Modifier
                        .padding(start = 8.dp, end = 8.dp, top = height * 0.22f)
                        .fillMaxWidth()
                        .height(height * 0.22f)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
fun Pill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    leading: String? = null
) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = (leading?.let { "$it " } ?: "") + text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
fun DuoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 48.dp,
    face: Color = AppTheme.colors.surface,
    shadow: Color = AppTheme.colors.border,
    bordered: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val c = AppTheme.colors
    DuoPressable(
        onClick = onClick,
        modifier = modifier.size(width = size, height = size + 4.dp),
        enabled = enabled,
        face = if (enabled) face else c.disabled,
        shadow = if (enabled) shadow else c.disabled,
        border = if (bordered) (if (enabled) shadow else c.disabled) else null,
        shape = RoundedCornerShape(14.dp),
        fillWidth = true,
        minHeight = size,
        content = content
    )
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colors.textMuted,
        modifier = modifier
    )
}

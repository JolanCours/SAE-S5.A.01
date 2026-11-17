package sae.app.sport.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightScheme = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = Blue,
    onSecondary = Color.White,
    tertiary = Orange,
    error = Red,
    background = LightAppColors.background,
    onBackground = LightAppColors.text,
    surface = LightAppColors.surface,
    onSurface = LightAppColors.text,
    surfaceVariant = LightAppColors.surfaceAlt,
    onSurfaceVariant = LightAppColors.textMuted,
    outline = LightAppColors.border
)

private val DarkScheme = darkColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = Blue,
    onSecondary = Color.White,
    tertiary = Orange,
    error = Red,
    background = DarkAppColors.background,
    onBackground = DarkAppColors.text,
    surface = DarkAppColors.surface,
    onSurface = DarkAppColors.text,
    surfaceVariant = DarkAppColors.surfaceAlt,
    onSurfaceVariant = DarkAppColors.textMuted,
    outline = DarkAppColors.border
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SAESportTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current
}

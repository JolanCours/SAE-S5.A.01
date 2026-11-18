package sae.app.sport.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Green = Color(0xFF58CC02)
val GreenDark = Color(0xFF58A700)
val GreenLight = Color(0xFFD7FFB8)
val Blue = Color(0xFF1CB0F6)
val BlueDark = Color(0xFF1899D6)
val BlueLight = Color(0xFFDDF4FF)
val Red = Color(0xFFFF4B4B)
val RedDark = Color(0xFFEA2B2B)
val RedLight = Color(0xFFFFDFE0)
val Orange = Color(0xFFFF9600)
val OrangeDark = Color(0xFFE08500)
val Yellow = Color(0xFFFFC800)
val YellowDark = Color(0xFFE5A800)
val Purple = Color(0xFFCE82FF)
val PurpleDark = Color(0xFFA568CC)
val Pink = Color(0xFFFF86D0)
val PinkDark = Color(0xFFE06CB4)

val Ink = Color(0xFF4B4B4B)
val InkMuted = Color(0xFF777777)
val Hare = Color(0xFFAFAFAF)
val Swan = Color(0xFFE5E5E5)
val Polar = Color(0xFFF7F7F7)

val CardPalette: List<Pair<Color, Color>> = listOf(
    Green to GreenDark,
    Blue to BlueDark,
    Orange to OrangeDark,
    Purple to PurpleDark,
    Pink to PinkDark,
    Yellow to YellowDark,
    Red to RedDark
)

fun cardColors(index: Int): Pair<Color, Color> = CardPalette[((index % CardPalette.size) + CardPalette.size) % CardPalette.size]

@Immutable
data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val border: Color,
    val text: Color,
    val textMuted: Color,
    val disabled: Color,
    val isDark: Boolean
)

val LightAppColors = AppColors(
    background = Color.White,
    surface = Color.White,
    surfaceAlt = Polar,
    border = Swan,
    text = Ink,
    textMuted = InkMuted,
    disabled = Swan,
    isDark = false
)

val DarkAppColors = AppColors(
    background = Color(0xFF131F24),
    surface = Color(0xFF131F24),
    surfaceAlt = Color(0xFF202F36),
    border = Color(0xFF37464F),
    text = Color(0xFFF1F7FB),
    textMuted = Color(0xFF8B9CA5),
    disabled = Color(0xFF37464F),
    isDark = true
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

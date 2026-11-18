package sae.app.sport.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import sae.app.sport.R

val Nunito = FontFamily(
    Font(R.font.nunito_semibold, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
    Font(R.font.nunito_black, FontWeight.Black)
)

private fun style(size: Int, weight: FontWeight, line: Int = (size * 1.25f).toInt(), spacing: Float = 0f) = TextStyle(
    fontFamily = Nunito,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = spacing.sp
)

val Typography = Typography(
    displayLarge = style(64, FontWeight.Black),
    displayMedium = style(48, FontWeight.Black),
    displaySmall = style(36, FontWeight.Black),
    headlineLarge = style(30, FontWeight.ExtraBold),
    headlineMedium = style(26, FontWeight.ExtraBold),
    headlineSmall = style(22, FontWeight.ExtraBold),
    titleLarge = style(20, FontWeight.ExtraBold),
    titleMedium = style(17, FontWeight.Bold),
    titleSmall = style(15, FontWeight.Bold),
    bodyLarge = style(17, FontWeight.SemiBold, 24),
    bodyMedium = style(15, FontWeight.SemiBold, 21),
    bodySmall = style(13, FontWeight.SemiBold, 18),
    labelLarge = style(15, FontWeight.ExtraBold, spacing = 0.8f),
    labelMedium = style(13, FontWeight.ExtraBold, spacing = 0.6f),
    labelSmall = style(11, FontWeight.ExtraBold, spacing = 0.6f)
)

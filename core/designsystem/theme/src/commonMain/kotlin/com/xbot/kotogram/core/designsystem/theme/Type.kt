package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.inter_medium
import com.xbot.kotogram.core.resources.inter_regular
import com.xbot.kotogram.core.resources.inter_semibold
import org.jetbrains.compose.resources.Font

@Composable
internal fun rememberKotogramTypography(laptop: Boolean): Typography {
    val fontFamily = if (currentPlatform.isApple) FontFamily.Default else interFontFamily()
    return remember(fontFamily, laptop) {
        if (laptop) desktopTypography(fontFamily) else mobileTypography(fontFamily)
    }
}

@Composable
private fun interFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
    Font(Res.font.inter_semibold, FontWeight.SemiBold),
)

@Suppress("MagicNumber", "LongMethod")
private fun mobileTypography(family: FontFamily) = Typography(
    displayLarge = family.style(57f, FontWeight.Normal, 10.5f),
    displayMedium = family.style(45f, FontWeight.Normal, 16.5f),
    displaySmall = family.style(36f, FontWeight.Normal, 21f),
    headlineLarge = family.style(28f, FontWeight.SemiBold, 28f),
    headlineMedium = family.style(23f, FontWeight.Normal, -9f),
    headlineSmall = family.style(20f, FontWeight.SemiBold, -46f),
    titleLarge = family.style(17f, FontWeight.SemiBold, -52f),
    titleMedium = family.style(16f, FontWeight.SemiBold, -40f),
    titleSmall = family.style(15f, FontWeight.Medium, -32f),
    bodyLarge = family.style(17f, FontWeight.Normal, -52f),
    bodyMedium = family.style(15f, FontWeight.Normal, -32f),
    bodySmall = family.style(13f, FontWeight.Normal, -12f),
    labelLarge = family.style(17f, FontWeight.SemiBold, -52f),
    labelMedium = family.style(14f, FontWeight.Normal, -22f),
    labelSmall = family.style(12f, FontWeight.Medium, 0f),
    displayLargeEmphasized = family.style(57f, FontWeight.Medium, 10.5f),
    displayMediumEmphasized = family.style(45f, FontWeight.Medium, 16.5f),
    displaySmallEmphasized = family.style(36f, FontWeight.Medium, 21f),
    headlineLargeEmphasized = family.style(28f, FontWeight.Bold, 28f),
    headlineMediumEmphasized = family.style(23f, FontWeight.Medium, -9f),
    headlineSmallEmphasized = family.style(20f, FontWeight.Bold, -46f),
    titleLargeEmphasized = family.style(17f, FontWeight.Bold, -52f),
    titleMediumEmphasized = family.style(16f, FontWeight.Bold, -40f),
    titleSmallEmphasized = family.style(15f, FontWeight.SemiBold, -32f),
    bodyLargeEmphasized = family.style(17f, FontWeight.Medium, -52f),
    bodyMediumEmphasized = family.style(15f, FontWeight.Medium, -32f),
    bodySmallEmphasized = family.style(13f, FontWeight.Medium, -12f),
    labelLargeEmphasized = family.style(17f, FontWeight.Bold, -52f),
    labelMediumEmphasized = family.style(14f, FontWeight.Medium, -22f),
    labelSmallEmphasized = family.style(12f, FontWeight.SemiBold, 0f),
)

@Suppress("MagicNumber", "LongMethod")
private fun desktopTypography(family: FontFamily) = Typography(
    displayLarge = family.style(57f, FontWeight.Normal, 10.5f),
    displayMedium = family.style(45f, FontWeight.Normal, 16.5f),
    displaySmall = family.style(36f, FontWeight.Normal, 21f),
    headlineLarge = family.style(22f, FontWeight.Medium, -24f),
    headlineMedium = family.style(24f, FontWeight.Medium, 6f),
    headlineSmall = family.style(18f, FontWeight.Medium, -50f),
    titleLarge = family.style(15f, FontWeight.Medium, -32f),
    titleMedium = family.style(14f, FontWeight.Medium, -22f),
    titleSmall = family.style(13.5f, FontWeight.Medium, -17f),
    bodyLarge = family.style(14f, FontWeight.Normal, -22f),
    bodyMedium = family.style(13f, FontWeight.Normal, -12f),
    bodySmall = family.style(12f, FontWeight.Normal, 0f),
    labelLarge = family.style(14f, FontWeight.Medium, -22f),
    labelMedium = family.style(12f, FontWeight.Normal, 0f),
    labelSmall = family.style(11f, FontWeight.Medium, 12f),
    displayLargeEmphasized = family.style(57f, FontWeight.Medium, 10.5f),
    displayMediumEmphasized = family.style(45f, FontWeight.Medium, 16.5f),
    displaySmallEmphasized = family.style(36f, FontWeight.Medium, 21f),
    headlineLargeEmphasized = family.style(22f, FontWeight.Medium, -24f),
    headlineMediumEmphasized = family.style(24f, FontWeight.SemiBold, 6f),
    headlineSmallEmphasized = family.style(18f, FontWeight.SemiBold, -50f),
    titleLargeEmphasized = family.style(15f, FontWeight.SemiBold, -32f),
    titleMediumEmphasized = family.style(14f, FontWeight.SemiBold, -22f),
    titleSmallEmphasized = family.style(13.5f, FontWeight.SemiBold, -17f),
    bodyLargeEmphasized = family.style(14f, FontWeight.Medium, -22f),
    bodyMediumEmphasized = family.style(13f, FontWeight.Medium, -12f),
    bodySmallEmphasized = family.style(12f, FontWeight.Medium, 0f),
    labelLargeEmphasized = family.style(14f, FontWeight.SemiBold, -22f),
    labelMediumEmphasized = family.style(12f, FontWeight.Medium, 0f),
    labelSmallEmphasized = family.style(11f, FontWeight.Medium, 12f),
)

// Workaround: tracking is SF Pro's trak at the size (units of 2048); Skia drops it and Inter is fitted to SF's widths.
private fun FontFamily.style(
    size: Float,
    weight: FontWeight,
    trak: Float,
): TextStyle = TextStyle(
    fontFamily = this,
    fontWeight = weight,
    fontSize = size.sp,
    letterSpacing = (
        trak /
            UNITS_PER_EM
        ).em,
)

private const val UNITS_PER_EM = 2048f

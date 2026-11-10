package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.inter_medium
import com.xbot.kotogram.core.resources.inter_regular
import com.xbot.kotogram.core.resources.inter_semibold
import org.jetbrains.compose.resources.Font

@Composable
internal fun rememberKotogramTypography(): Typography {
    val fontFamily = if (currentPlatform.isApple) FontFamily.Default else interFontFamily()
    return remember(fontFamily) { kotogramTypography(fontFamily) }
}

@Composable
private fun interFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
    Font(Res.font.inter_semibold, FontWeight.SemiBold),
)

@Suppress("MagicNumber")
private fun kotogramTypography(fontFamily: FontFamily): Typography {
    val baseline = Typography()
    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = baseline.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = baseline.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = textStyle(fontFamily, 22, FontWeight.Medium),
        titleLarge = textStyle(fontFamily, 18, FontWeight.Medium),
        titleMedium = textStyle(fontFamily, 15, FontWeight.Medium),
        titleSmall = textStyle(fontFamily, 14, FontWeight.Medium),
        bodyLarge = textStyle(fontFamily, 14),
        bodyMedium = textStyle(fontFamily, 13),
        bodySmall = textStyle(fontFamily, 12),
        labelLarge = textStyle(fontFamily, 14, FontWeight.Medium),
        labelMedium = textStyle(fontFamily, 12, FontWeight.Medium),
        labelSmall = textStyle(fontFamily, 11, FontWeight.Medium),
    )
}

private fun textStyle(
    fontFamily: FontFamily,
    size: Int,
    weight: FontWeight = FontWeight.Normal,
): TextStyle = TextStyle(fontFamily = fontFamily, fontWeight = weight, fontSize = size.sp)

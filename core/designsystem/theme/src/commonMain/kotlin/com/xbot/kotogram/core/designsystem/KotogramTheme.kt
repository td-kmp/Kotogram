package com.xbot.kotogram.core.designsystem

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import com.xbot.kotogram.core.designsystem.media.LocalUiMediaScope
import com.xbot.kotogram.core.designsystem.style.LocalMediaQueryInfo
import com.xbot.kotogram.core.designsystem.theme.DayClassicColorScheme
import com.xbot.kotogram.core.designsystem.theme.DimIndication
import com.xbot.kotogram.core.designsystem.theme.KotogramShapes
import com.xbot.kotogram.core.designsystem.theme.KotogramSpacing
import com.xbot.kotogram.core.designsystem.theme.LocalKotogramSpacing
import com.xbot.kotogram.core.designsystem.theme.NightAccentColorScheme
import com.xbot.kotogram.core.designsystem.theme.currentPlatform
import com.xbot.kotogram.core.designsystem.theme.platformDynamicColorScheme
import com.xbot.kotogram.core.designsystem.theme.platformMediaQueryInfo
import com.xbot.kotogram.core.designsystem.theme.platformUiMediaScope
import com.xbot.kotogram.core.designsystem.theme.rememberKotogramTypography

@Composable
fun KotogramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = kotogramColorScheme(darkTheme, dynamicColor)
    val spacing = remember { KotogramSpacing() }
    CompositionLocalProvider(
        LocalKotogramSpacing provides spacing,
        LocalUiMediaScope provides platformUiMediaScope(),
        LocalMediaQueryInfo provides platformMediaQueryInfo(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = rememberKotogramTypography(),
            shapes = KotogramShapes,
        ) {
            CompositionLocalProvider(*indicationOverrides(), content = content)
        }
    }
}

private fun indicationOverrides(): Array<ProvidedValue<*>> = if (currentPlatform.isApple) {
    arrayOf(LocalIndication provides DimIndication, LocalRippleConfiguration provides null)
} else {
    emptyArray()
}

@Composable
internal fun kotogramColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
): ColorScheme {
    val dynamicColorScheme = if (dynamicColor) platformDynamicColorScheme(darkTheme) else null
    return dynamicColorScheme ?: if (darkTheme) NightAccentColorScheme else DayClassicColorScheme
}

object KotogramTheme {
    val colors: ColorScheme
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val spacing: KotogramSpacing
        @Composable @ReadOnlyComposable
        get() = LocalKotogramSpacing.current

    val indication: Indication
        @Composable
        get() = if (currentPlatform.isApple) DimIndication else ripple()

    val values: Values
        @Composable
        get() {
            val colors = colors
            val typography = typography
            val shapes = shapes
            val spacing = spacing
            return remember(colors, typography, shapes, spacing) {
                Values(colors, typography, shapes, spacing)
            }
        }

    @Immutable
    class Values(
        val colors: ColorScheme,
        val typography: Typography,
        val shapes: Shapes,
        val spacing: KotogramSpacing,
    )
}

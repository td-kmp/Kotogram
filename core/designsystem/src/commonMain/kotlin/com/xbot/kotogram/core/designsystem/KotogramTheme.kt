package com.xbot.kotogram.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import com.xbot.kotogram.core.designsystem.theme.DayClassicColorScheme
import com.xbot.kotogram.core.designsystem.theme.KotogramShapes
import com.xbot.kotogram.core.designsystem.theme.KotogramSpacing
import com.xbot.kotogram.core.designsystem.theme.LocalKotogramSpacing
import com.xbot.kotogram.core.designsystem.theme.NightAccentColorScheme
import com.xbot.kotogram.core.designsystem.theme.platformDynamicColorScheme
import com.xbot.kotogram.core.designsystem.theme.rememberKotogramTypography

@Composable
fun KotogramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val dynamicColorScheme = if (dynamicColor) platformDynamicColorScheme(darkTheme) else null
    val colorScheme = dynamicColorScheme ?: if (darkTheme) NightAccentColorScheme else DayClassicColorScheme
    val spacing = remember { KotogramSpacing() }
    CompositionLocalProvider(LocalKotogramSpacing provides spacing) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = rememberKotogramTypography(),
            shapes = KotogramShapes,
            content = content
        )
    }
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
}

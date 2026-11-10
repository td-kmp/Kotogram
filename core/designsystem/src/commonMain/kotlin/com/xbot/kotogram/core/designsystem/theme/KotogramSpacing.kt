package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class KotogramSpacing(
    val extraSmall: Dp = 5.dp,
    val small: Dp = 10.dp,
    val medium: Dp = 15.dp,
    val large: Dp = 20.dp,
    val extraLarge: Dp = 40.dp,
)

internal val LocalKotogramSpacing = staticCompositionLocalOf { KotogramSpacing() }

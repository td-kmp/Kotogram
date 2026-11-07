package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
internal actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

internal actual val platformSystemFont: Boolean = System.getProperty("os.name").orEmpty().startsWith("Mac")

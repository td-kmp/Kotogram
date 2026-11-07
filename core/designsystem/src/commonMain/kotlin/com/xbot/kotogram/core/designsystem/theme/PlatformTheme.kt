package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
internal expect fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme?

// Apple's SF Pro can't be bundled; Compose's default family is the system's .AppleSystemUIFont on Apple platforms.
internal expect val platformSystemFont: Boolean

internal expect val platformPressHighlight: Boolean

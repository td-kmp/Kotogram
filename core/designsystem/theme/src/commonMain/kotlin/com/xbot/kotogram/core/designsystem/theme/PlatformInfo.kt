package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo

@InternalDesignSystemApi
class PlatformInfo internal constructor(
    val isApple: Boolean,
    val isDesktop: Boolean,
)

@InternalDesignSystemApi
expect val currentPlatform: PlatformInfo

@Composable
internal expect fun platformUiMediaScope(): UiMediaScope

@Composable
internal expect fun platformMediaQueryInfo(): MediaQueryInfo

@Composable
internal expect fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme?

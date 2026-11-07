package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import com.xbot.kotogram.core.designsystem.media.UiMediaScope

internal class PlatformInfo(val isApple: Boolean, val isDesktop: Boolean)

internal expect val currentPlatform: PlatformInfo

@Composable
internal expect fun platformComponentOverrides(): Array<ProvidedValue<*>>

@Composable
internal expect fun platformUiMediaScope(): UiMediaScope

@Composable
internal expect fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme?

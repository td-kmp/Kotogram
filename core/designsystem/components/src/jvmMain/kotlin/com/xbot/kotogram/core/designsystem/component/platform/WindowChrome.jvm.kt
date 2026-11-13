package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import dev.nucleusframework.window.LocalWindowChromeInsets

@Composable
internal actual fun windowChrome(): WindowChrome? {
    val insets = LocalWindowChromeInsets.current
    if (insets.titleBarHeight == 0.dp) return null
    val direction = LocalLayoutDirection.current
    return with(LocalDensity.current) {
        WindowChrome(
            height = insets.titleBarHeight.roundToPx(),
            start = insets.controlsInsets.calculateStartPadding(direction).roundToPx(),
            end = insets.controlsInsets.calculateEndPadding(direction).roundToPx(),
        )
    }
}

package com.xbot.kotogram.core.designsystem.window

import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.WindowInfo
import dev.nucleusframework.window.DecoratedWindowScope

@Stable
class KotogramWindowInfo internal constructor(
    private val base: WindowInfo,
    private val window: DecoratedWindowScope,
) : WindowInfo by base {
    // Workaround: Nucleus puts a minimize into its window state, not into the window's focus.
    override val isWindowFocused: Boolean get() = window.state.isActive && !window.state.isMinimized

    val isMinimized: Boolean get() = window.state.isMinimized

    val isMaximized: Boolean get() = window.state.isMaximized

    val isFullscreen: Boolean get() = window.state.isFullscreen
}

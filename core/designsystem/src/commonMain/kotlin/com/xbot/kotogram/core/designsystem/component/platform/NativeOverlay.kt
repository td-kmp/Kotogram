package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

// The window's lifecycle, under every screen's own: KotogramTheme takes it at the root.
internal val LocalWindowLifecycle = staticCompositionLocalOf<Lifecycle?> { null }

@Composable
internal fun isSettledInFront(): Boolean {
    val screen by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val windowLifecycle = LocalWindowLifecycle.current ?: LocalLifecycleOwner.current.lifecycle
    val window by windowLifecycle.currentStateAsState()
    // Backgrounded, the whole window drops; the app switcher's snapshot still needs the glass.
    return screen.isAtLeast(Lifecycle.State.RESUMED) ||
        (window < Lifecycle.State.RESUMED && screen >= window)
}

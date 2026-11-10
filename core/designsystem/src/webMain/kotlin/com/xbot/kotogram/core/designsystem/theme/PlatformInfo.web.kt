package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.media.WebMediaScope
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import kotlinx.browser.window

internal actual val currentPlatform: PlatformInfo = PlatformInfo(isApple = false, isDesktop = true)

@Composable
internal actual fun platformComponentOverrides(): Array<ProvidedValue<*>> = emptyArray()

@Composable
internal actual fun platformUiMediaScope(): UiMediaScope {
    val windowInfo = LocalWindowInfo.current
    val scope = remember(windowInfo) { WebMediaScope(windowInfo) }
    DisposableEffect(scope) { onDispose(scope::dispose) }
    return scope
}

// Workaround: a browser reports no device type, so a fine pointer that hovers counts as a computer.
@Composable
internal actual fun platformMediaQueryInfo(): MediaQueryInfo = remember {
    MediaQueryInfo(
        isLaptop = window.matchMedia("(any-pointer: fine) and (any-hover: hover)").matches,
        isTv = false,
        isAuto = false,
    )
}

@Composable
internal actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

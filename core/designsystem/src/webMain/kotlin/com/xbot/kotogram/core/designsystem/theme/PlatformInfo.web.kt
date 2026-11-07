package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import com.xbot.kotogram.core.designsystem.media.Pc
import com.xbot.kotogram.core.designsystem.media.PlatformUiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.KeyboardKind
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PlatformFeature
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PointerPrecision
import kotlinx.browser.window

internal actual val currentPlatform: PlatformInfo = PlatformInfo(isApple = false, isDesktop = true)

@Composable
internal actual fun platformComponentOverrides(): Array<ProvidedValue<*>> = emptyArray()

// CSS media features: a fine pointer that hovers counts as a computer, with a keyboard of its own.
@Composable
internal actual fun platformUiMediaScope(): UiMediaScope {
    val windowInfo = LocalWindowInfo.current
    return remember(windowInfo) {
        val computer = matches("(any-pointer: fine)") && matches("(any-hover: hover)")
        PlatformUiMediaScope(
            windowInfo = windowInfo,
            pointerPrecision = when {
                matches("(any-pointer: fine)") -> PointerPrecision.Fine
                matches("(any-pointer: coarse)") -> PointerPrecision.Coarse
                else -> PointerPrecision.None
            },
            keyboardKind = if (computer) KeyboardKind.Physical else KeyboardKind.None,
            platformFeatures = if (computer) setOf(PlatformFeature.Pc) else emptySet()
        )
    }
}

private fun matches(query: String): Boolean = window.matchMedia(query).matches

@Composable
internal actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

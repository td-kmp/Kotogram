package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import com.xbot.kotogram.core.designsystem.media.IosUiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import platform.Foundation.NSProcessInfo
import platform.Foundation.iOSAppOnMac

@InternalDesignSystemApi
actual val currentPlatform: PlatformInfo = PlatformInfo(isApple = true, isDesktop = false)

@Composable
internal actual fun platformUiMediaScope(): UiMediaScope {
    val windowInfo = LocalWindowInfo.current
    val scope = remember(windowInfo) { IosUiMediaScope(windowInfo) }
    DisposableEffect(scope) {
        scope.startObserving()
        onDispose(scope::stopObserving)
    }
    return scope
}

@Composable
internal actual fun platformMediaQueryInfo(): MediaQueryInfo = IosMediaQueryInfo

private val IosMediaQueryInfo =
    MediaQueryInfo(isLaptop = NSProcessInfo.processInfo.iOSAppOnMac, isTv = false, isAuto = false)

@Composable
internal actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

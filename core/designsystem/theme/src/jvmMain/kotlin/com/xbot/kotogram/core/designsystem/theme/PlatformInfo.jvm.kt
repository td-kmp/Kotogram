package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import com.xbot.kotogram.core.designsystem.media.DesktopMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo

@InternalDesignSystemApi
actual val currentPlatform: PlatformInfo =
    PlatformInfo(isApple = System.getProperty("os.name").orEmpty().startsWith("Mac"), isDesktop = true)

@Composable
internal actual fun platformUiMediaScope(): UiMediaScope {
    val windowInfo = LocalWindowInfo.current
    return remember(windowInfo) { DesktopMediaScope(windowInfo) }
}

@Composable
internal actual fun platformMediaQueryInfo(): MediaQueryInfo = DesktopMediaQueryInfo

private val DesktopMediaQueryInfo = MediaQueryInfo(isLaptop = true, isTv = false, isAuto = false)

@Composable
internal actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

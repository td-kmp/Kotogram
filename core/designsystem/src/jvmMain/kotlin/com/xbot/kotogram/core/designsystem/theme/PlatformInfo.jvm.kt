package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import com.xbot.kotogram.core.designsystem.component.LocalCheckboxOverride
import com.xbot.kotogram.core.designsystem.component.LocalGlassIconButtonOverride
import com.xbot.kotogram.core.designsystem.component.LocalMenuOverride
import com.xbot.kotogram.core.designsystem.component.LocalProgressIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.LocalProminentButtonOverride
import com.xbot.kotogram.core.designsystem.component.LocalSwitchOverride
import com.xbot.kotogram.core.designsystem.component.platform.MacCheckboxOverride
import com.xbot.kotogram.core.designsystem.component.platform.MacGlassIconButtonOverride
import com.xbot.kotogram.core.designsystem.component.platform.MacGlassProminentButtonOverride
import com.xbot.kotogram.core.designsystem.component.platform.MacPopUpMenuOverride
import com.xbot.kotogram.core.designsystem.component.platform.MacSpinnerOverride
import com.xbot.kotogram.core.designsystem.component.platform.MacSwitchOverride
import com.xbot.kotogram.core.designsystem.media.DesktopMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.native.MacGlassButton
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import dev.nucleusframework.window.tao.LocalTaoWindow

internal actual val currentPlatform: PlatformInfo =
    PlatformInfo(isApple = System.getProperty("os.name").orEmpty().startsWith("Mac"), isDesktop = true)

@Composable
internal actual fun platformComponentOverrides(): Array<ProvidedValue<*>> {
    if (!currentPlatform.isApple || LocalTaoWindow.current == null) return emptyArray()
    return listOfNotNull(
        LocalProgressIndicatorOverride provides MacSpinnerOverride,
        LocalMenuOverride provides MacPopUpMenuOverride,
        LocalSwitchOverride provides MacSwitchOverride,
        LocalCheckboxOverride provides MacCheckboxOverride,
        if (MacGlassButton.isSupported) LocalProminentButtonOverride provides MacGlassProminentButtonOverride else null,
        if (MacGlassButton.isSupported) LocalGlassIconButtonOverride provides MacGlassIconButtonOverride else null,
    ).toTypedArray()
}

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

package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import com.xbot.kotogram.core.designsystem.component.IndexFastScrollerOverride
import com.xbot.kotogram.core.designsystem.component.LocalBottomSheetOverride
import com.xbot.kotogram.core.designsystem.component.LocalFastScrollerOverride
import com.xbot.kotogram.core.designsystem.component.LocalGlassIconButtonOverride
import com.xbot.kotogram.core.designsystem.component.LocalProgressIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.LocalProminentButtonOverride
import com.xbot.kotogram.core.designsystem.component.LocalSearchFieldOverride
import com.xbot.kotogram.core.designsystem.component.LocalSwitchOverride
import com.xbot.kotogram.core.designsystem.component.platform.ActivityIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.platform.GlassProminentButtonOverride
import com.xbot.kotogram.core.designsystem.component.platform.GlassSearchFieldOverride
import com.xbot.kotogram.core.designsystem.component.platform.IosGlassIconButtonOverride
import com.xbot.kotogram.core.designsystem.component.platform.IosSwitchOverride
import com.xbot.kotogram.core.designsystem.component.platform.PageSheetOverride
import com.xbot.kotogram.core.designsystem.component.platform.hasLiquidGlass
import com.xbot.kotogram.core.designsystem.media.IosUiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import platform.Foundation.NSProcessInfo
import platform.Foundation.iOSAppOnMac

internal actual val currentPlatform: PlatformInfo = PlatformInfo(isApple = true, isDesktop = false)

@Composable
internal actual fun platformComponentOverrides(): Array<ProvidedValue<*>> = listOfNotNull(
    LocalProgressIndicatorOverride provides ActivityIndicatorOverride,
    LocalBottomSheetOverride provides PageSheetOverride,
    LocalFastScrollerOverride provides IndexFastScrollerOverride,
    LocalSwitchOverride provides IosSwitchOverride,
    if (hasLiquidGlass) LocalProminentButtonOverride provides GlassProminentButtonOverride else null,
    if (hasLiquidGlass) LocalGlassIconButtonOverride provides IosGlassIconButtonOverride else null,
    if (hasLiquidGlass) LocalSearchFieldOverride provides GlassSearchFieldOverride else null,
).toTypedArray()

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

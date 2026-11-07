package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import com.xbot.kotogram.core.designsystem.component.LocalProgressIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.LocalProminentButtonOverride
import com.xbot.kotogram.core.designsystem.component.platform.ActivityIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.platform.GlassProminentButtonOverride
import com.xbot.kotogram.core.designsystem.component.platform.hasLiquidGlass
import com.xbot.kotogram.core.designsystem.media.Camera
import com.xbot.kotogram.core.designsystem.media.Microphone
import com.xbot.kotogram.core.designsystem.media.PlatformUiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.KeyboardKind
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PlatformFeature
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PointerPrecision
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIKeyboardWillHideNotification
import platform.UIKit.UIKeyboardWillShowNotification

internal actual val currentPlatform: PlatformInfo = PlatformInfo(isApple = true, isDesktop = false)

@Composable
internal actual fun platformComponentOverrides(): Array<ProvidedValue<*>> = listOfNotNull(
    LocalProgressIndicatorOverride provides ActivityIndicatorOverride,
    if (hasLiquidGlass) LocalProminentButtonOverride provides GlassProminentButtonOverride else null
).toTypedArray()

// Touch first, as an iPad stays with a trackpad attached; the keyboard is the on-screen one while UIKit shows it.
@Composable
internal actual fun platformUiMediaScope(): UiMediaScope {
    val windowInfo = LocalWindowInfo.current
    val scope = remember(windowInfo) {
        PlatformUiMediaScope(
            windowInfo = windowInfo,
            pointerPrecision = PointerPrecision.Coarse,
            keyboardKind = KeyboardKind.None,
            platformFeatures = setOf(PlatformFeature.Camera, PlatformFeature.Microphone)
        )
    }
    DisposableEffect(scope) {
        val center = NSNotificationCenter.defaultCenter
        val observers = listOf(
            UIKeyboardWillShowNotification to KeyboardKind.Virtual,
            UIKeyboardWillHideNotification to KeyboardKind.None
        ).map { (name, kind) ->
            center.addObserverForName(name, null, NSOperationQueue.mainQueue) { scope.keyboardKind = kind }
        }
        onDispose { observers.forEach(center::removeObserver) }
    }
    return scope
}

@Composable
internal actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

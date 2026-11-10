package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.Foundation.NSOperatingSystemVersion
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView

@OptIn(ExperimentalComposeUiApi::class)
internal val GlassProperties =
    UIKitInteropProperties(isNativeAccessibilityEnabled = true, placedAsOverlay = true)

@OptIn(ExperimentalForeignApi::class)
internal val hasLiquidGlass: Boolean = NSProcessInfo.processInfo.isOperatingSystemAtLeastVersion(
    cValue<NSOperatingSystemVersion> {
        majorVersion = LIQUID_GLASS_IOS_VERSION
        minorVersion = 0
        patchVersion = 0
    },
)

internal fun fade(
    view: UIView,
    visible: Boolean,
) {
    val alpha = if (visible) 1.0 else 0.0
    if (view.alpha != alpha) UIView.animateWithDuration(FADE_SECONDS) { view.alpha = alpha }
}

internal fun interfaceStyle(dark: Boolean): UIUserInterfaceStyle = if (dark) {
    UIUserInterfaceStyle.UIUserInterfaceStyleDark
} else {
    UIUserInterfaceStyle.UIUserInterfaceStyleLight
}

private const val LIQUID_GLASS_IOS_VERSION = 26L

private const val FADE_SECONDS = 0.2

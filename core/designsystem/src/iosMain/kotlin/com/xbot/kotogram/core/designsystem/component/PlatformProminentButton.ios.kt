package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.extensions.appleWeight
import com.xbot.kotogram.core.designsystem.extensions.containerColor
import com.xbot.kotogram.core.designsystem.extensions.contentColor
import com.xbot.kotogram.core.designsystem.extensions.isDark
import com.xbot.kotogram.core.designsystem.extensions.toUIColor
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.Foundation.NSAttributedString
import platform.Foundation.NSOperatingSystemVersion
import platform.Foundation.NSProcessInfo
import platform.Foundation.create
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIAction
import platform.UIKit.UIButton
import platform.UIKit.UIButtonConfiguration
import platform.UIKit.UIButtonConfigurationCornerStyleCapsule
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel

@Composable
internal actual fun PlatformProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    state: ButtonState,
    colors: ButtonColors,
    contentPadding: PaddingValues
) {
    if (hasLiquidGlass) {
        GlassProminentButton(text, onClick, modifier, state, colors, contentPadding)
    } else {
        FilledProminentButton(text, onClick, modifier, state, colors, contentPadding)
    }
}

@Composable
private fun GlassProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    state: ButtonState,
    colors: ButtonColors,
    contentPadding: PaddingValues
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val style = KotogramTheme.typography.labelLarge
    val fontSize = with(LocalDensity.current) { style.fontSize.toDp().value.toDouble() }
    val dark = KotogramTheme.colors.background.isDark()
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val inFront = lifecycle.isAtLeast(Lifecycle.State.RESUMED)
    NativeProminentButtonBox(text, colors, contentPadding, modifier) {
        UIKitView(
            factory = {
                UIButton.buttonWithConfiguration(
                    configuration = UIButtonConfiguration.prominentGlassButtonConfiguration(),
                    primaryAction = UIAction.actionWithHandler { currentOnClick() }
                ).apply { alpha = 0.0 }
            },
            modifier = Modifier.matchParentSize(),
            update = { button ->
                button.configuration = glassConfiguration(
                    title = text.takeUnless { state == ButtonState.Loading },
                    font = UIFont.systemFontOfSize(fontSize, style.fontWeight.appleWeight()),
                    color = colors.contentColor(state).toUIColor(),
                    loading = state == ButtonState.Loading
                )
                button.tintColor = colors.containerColor(state).toUIColor()
                button.overrideUserInterfaceStyle = if (dark) {
                    UIUserInterfaceStyle.UIUserInterfaceStyleDark
                } else {
                    UIUserInterfaceStyle.UIUserInterfaceStyleLight
                }
                button.userInteractionEnabled = state == ButtonState.Enabled && inFront
                button.accessibilityLabel = text
                fade(button, visible = inFront)
            },
            properties = GlassProperties
        )
    }
}

@OptIn(BetaInteropApi::class)
private fun glassConfiguration(title: String?, font: UIFont, color: UIColor, loading: Boolean): UIButtonConfiguration =
    UIButtonConfiguration.prominentGlassButtonConfiguration().apply {
        attributedTitle = title?.let {
            NSAttributedString.create(
                string = it,
                attributes = mapOf<Any?, Any?>(NSFontAttributeName to font, NSForegroundColorAttributeName to color)
            )
        }
        baseForegroundColor = color
        showsActivityIndicator = loading
        cornerStyle = UIButtonConfigurationCornerStyleCapsule
    }

private fun fade(view: UIView, visible: Boolean) {
    val alpha = if (visible) 1.0 else 0.0
    if (view.alpha != alpha) UIView.animateWithDuration(FADE_SECONDS) { view.alpha = alpha }
}

@OptIn(ExperimentalComposeUiApi::class)
private val GlassProperties =
    UIKitInteropProperties(isNativeAccessibilityEnabled = true, placedAsOverlay = true)

@OptIn(ExperimentalForeignApi::class)
private val hasLiquidGlass: Boolean = NSProcessInfo.processInfo.isOperatingSystemAtLeastVersion(
    cValue<NSOperatingSystemVersion> {
        majorVersion = LIQUID_GLASS_IOS_VERSION
        minorVersion = 0
        patchVersion = 0
    }
)

private const val LIQUID_GLASS_IOS_VERSION = 26L

private const val FADE_SECONDS = 0.2

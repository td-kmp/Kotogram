package com.xbot.kotogram.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.extensions.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.Foundation.NSOperatingSystemVersion
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIAction
import platform.UIKit.UIButton
import platform.UIKit.UIButtonConfiguration
import platform.UIKit.UIButtonConfigurationCornerStyleCapsule
import platform.UIKit.UIButtonConfigurationSize
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel

@Composable
internal actual fun PlatformProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    state: ButtonState
) {
    if (hasLiquidGlass) {
        GlassProminentButton(text, onClick, modifier, state)
    } else {
        FilledProminentButton(text, onClick, modifier, state)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun GlassProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    state: ButtonState
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val tint = KotogramTheme.colors.primary.toUIColor()
    val style = if (KotogramTheme.colors.background.luminance() < HALF_LUMINANCE) {
        UIUserInterfaceStyle.UIUserInterfaceStyleDark
    } else {
        UIUserInterfaceStyle.UIUserInterfaceStyleLight
    }
    // An overlay covers whatever Compose draws over it, so the glass shows only while its screen is
    // settled in front: NavDisplay holds a screen at STARTED during transitions and under dialogs.
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val inFront = lifecycle.isAtLeast(Lifecycle.State.RESUMED)
    val remeasure = rememberUIKitInteropRemeasureRequester()
    UIKitView(
        factory = {
            UIButton.buttonWithConfiguration(
                configuration = UIButtonConfiguration.prominentGlassButtonConfiguration(),
                primaryAction = UIAction.actionWithHandler { currentOnClick() }
            ).apply { alpha = 0.0 }
        },
        modifier = modifier
            .then(KeepTitleSizeElement(loading = state == ButtonState.Loading))
            .remeasureRequester(remeasure),
        update = { button ->
            button.configuration = glassConfiguration(text, state)
            button.tintColor = tint
            button.overrideUserInterfaceStyle = style
            button.enabled = state != ButtonState.Disabled
            button.userInteractionEnabled = state == ButtonState.Enabled && inFront
            button.accessibilityLabel = text
            fade(button, visible = inFront)
            remeasure.requestRemeasure()
        },
        properties = GlassProperties
    )
}

private fun glassConfiguration(text: String, state: ButtonState): UIButtonConfiguration =
    UIButtonConfiguration.prominentGlassButtonConfiguration().apply {
        title = text.takeUnless { state == ButtonState.Loading }
        showsActivityIndicator = state == ButtonState.Loading
        buttonSize = UIButtonConfigurationSize.UIButtonConfigurationSizeLarge
        cornerStyle = UIButtonConfigurationCornerStyleCapsule
    }

private fun fade(view: UIView, visible: Boolean) {
    val alpha = if (visible) 1.0 else 0.0
    if (view.alpha != alpha) UIView.animateWithDuration(FADE_SECONDS) { view.alpha = alpha }
}

// UIKit's spinner alone is smaller than the title: like Button, a loading button keeps its title's size.
private data class KeepTitleSizeElement(val loading: Boolean) :
    ModifierNodeElement<KeepTitleSizeNode>() {
    override fun create(): KeepTitleSizeNode = KeepTitleSizeNode(loading)

    override fun update(node: KeepTitleSizeNode) {
        node.loading = loading
        node.invalidateMeasurement()
    }
}

private class KeepTitleSizeNode(var loading: Boolean) :
    Modifier.Node(),
    LayoutModifierNode {
    private var titleWidth = 0
    private var titleHeight = 0

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(
            if (loading) {
                constraints.copy(
                    minWidth = titleWidth.coerceIn(constraints.minWidth, constraints.maxWidth),
                    minHeight = titleHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
                )
            } else {
                constraints
            }
        )
        if (!loading) {
            titleWidth = placeable.width
            titleHeight = placeable.height
        }
        return layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
}

// The glass refracts what lies under it, so it has to sit above Compose's canvas.
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

private const val HALF_LUMINANCE = 0.5f

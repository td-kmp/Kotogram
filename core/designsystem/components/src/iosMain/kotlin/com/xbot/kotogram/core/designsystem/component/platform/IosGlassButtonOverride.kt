package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.GlassButtonOverride
import com.xbot.kotogram.core.designsystem.component.GlassButtonOverrideScope
import com.xbot.kotogram.core.designsystem.component.NativeBox
import com.xbot.kotogram.core.designsystem.extensions.appleWeight
import com.xbot.kotogram.core.designsystem.extensions.containerColor
import com.xbot.kotogram.core.designsystem.extensions.contentColor
import com.xbot.kotogram.core.designsystem.extensions.isDark
import com.xbot.kotogram.core.designsystem.extensions.toUIColor
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSAttributedString
import platform.Foundation.create
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIAction
import platform.UIKit.UIButton
import platform.UIKit.UIButtonConfiguration
import platform.UIKit.UIButtonConfigurationCornerStyleCapsule
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.accessibilityLabel

internal object IosGlassButtonOverride : GlassButtonOverride {
    @Composable
    override fun GlassButtonOverrideScope.GlassButton() {
        val currentOnClick by rememberUpdatedState(onClick)
        val textStyle = buttonStyle.textStyle
        val fontSize = with(LocalDensity.current) { textStyle.fontSize.toDp().value.toDouble() }
        val dark = KotogramTheme.colors.surface.isDark()
        NativeBox {
            OverlayView(
                factory = {
                    UIButton.buttonWithConfiguration(
                        configuration = UIButtonConfiguration.prominentGlassButtonConfiguration(),
                        primaryAction = UIAction.actionWithHandler { currentOnClick() },
                    )
                },
                modifier = Modifier.matchParentSize(),
                update = { button ->
                    val tint = colors.containerColor(state)
                    button.configuration = glassConfiguration(
                        prominent = tint.alpha > 0f,
                        title = text.takeUnless { state == Loading },
                        font = UIFont.systemFontOfSize(fontSize, textStyle.fontWeight.appleWeight()),
                        color = colors.contentColor(state).toUIColor(),
                        loading = state == Loading,
                    )
                    if (tint.alpha > 0f) button.tintColor = tint.toUIColor()
                    button.overrideUserInterfaceStyle = interfaceStyle(dark)
                    button.userInteractionEnabled = state == Enabled
                    button.accessibilityLabel = text
                },
                properties = GlassProperties,
            )
        }
    }
}

@OptIn(BetaInteropApi::class)
private fun glassConfiguration(
    prominent: Boolean,
    title: String?,
    font: UIFont,
    color: UIColor,
    loading: Boolean,
): UIButtonConfiguration = if (prominent) {
    UIButtonConfiguration.prominentGlassButtonConfiguration()
} else {
    UIButtonConfiguration.glassButtonConfiguration()
}.apply {
    attributedTitle = title?.let {
        NSAttributedString.create(
            string = it,
            attributes = mapOf<Any?, Any?>(NSFontAttributeName to font, NSForegroundColorAttributeName to color),
        )
    }
    baseForegroundColor = color
    showsActivityIndicator = loading
    cornerStyle = UIButtonConfigurationCornerStyleCapsule
}

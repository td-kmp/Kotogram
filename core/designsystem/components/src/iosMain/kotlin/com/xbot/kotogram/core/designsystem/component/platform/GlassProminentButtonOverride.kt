package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.NativeBox
import com.xbot.kotogram.core.designsystem.component.ProminentButtonOverride
import com.xbot.kotogram.core.designsystem.component.ProminentButtonOverrideScope
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

internal object GlassProminentButtonOverride : ProminentButtonOverride {
    @Composable
    override fun ProminentButtonOverrideScope.ProminentButton() {
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
                    button.configuration = glassConfiguration(
                        title = text.takeUnless { state == ButtonState.Loading },
                        font = UIFont.systemFontOfSize(fontSize, textStyle.fontWeight.appleWeight()),
                        color = colors.contentColor(state).toUIColor(),
                        loading = state == ButtonState.Loading,
                    )
                    button.tintColor = colors.containerColor(state).toUIColor()
                    button.overrideUserInterfaceStyle = interfaceStyle(dark)
                    button.userInteractionEnabled = state == ButtonState.Enabled
                    button.accessibilityLabel = text
                },
                properties = GlassProperties,
            )
        }
    }
}

@OptIn(BetaInteropApi::class)
private fun glassConfiguration(
    title: String?,
    font: UIFont,
    color: UIColor,
    loading: Boolean,
): UIButtonConfiguration = UIButtonConfiguration.prominentGlassButtonConfiguration().apply {
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

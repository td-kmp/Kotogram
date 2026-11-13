package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.foundation.layout.size
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.GlassIconButtonOverride
import com.xbot.kotogram.core.designsystem.component.GlassIconButtonOverrideScope
import com.xbot.kotogram.core.designsystem.extensions.isDark
import com.xbot.kotogram.core.designsystem.extensions.toUIColor
import com.xbot.kotogram.core.designsystem.extensions.toUIImage
import platform.UIKit.UIAction
import platform.UIKit.UIButton
import platform.UIKit.UIButtonConfiguration
import platform.UIKit.UIButtonConfigurationCornerStyleCapsule
import platform.UIKit.UIImageRenderingMode
import platform.UIKit.accessibilityLabel

internal object IosGlassIconButtonOverride : GlassIconButtonOverride {
    @Composable
    override fun GlassIconButtonOverrideScope.GlassIconButton() {
        val currentOnClick by rememberUpdatedState(onClick)
        val bitmap = rememberIconBitmap(icon, style.iconSize, Color.Black)
        val scale = LocalDensity.current.density
        val templateImage = remember(bitmap, scale) {
            bitmap.toUIImage(scale)?.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate)
        }
        val color = iconColor.toUIColor()
        val tint = style.containerColor.takeIf { it.alpha > 0f }?.toUIColor()
        val dark = KotogramTheme.colors.surface.isDark()
        OverlayView(
            factory = {
                UIButton.buttonWithConfiguration(
                    configuration = UIButtonConfiguration.glassButtonConfiguration(),
                    primaryAction = UIAction.actionWithHandler { currentOnClick() },
                )
            },
            modifier = modifier.minimumInteractiveComponentSize().size(style.size),
            update = { button ->
                val configuration = if (tint != null) {
                    UIButtonConfiguration.prominentGlassButtonConfiguration()
                } else {
                    UIButtonConfiguration.glassButtonConfiguration()
                }
                button.configuration = configuration.apply {
                    image = templateImage
                    baseForegroundColor = color
                    cornerStyle = UIButtonConfigurationCornerStyleCapsule
                }
                tint?.let { button.tintColor = it }
                button.overrideUserInterfaceStyle = interfaceStyle(dark)
                button.enabled = enabled
                button.accessibilityLabel = contentDescription
            },
            properties = GlassProperties,
        )
    }
}

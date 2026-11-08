package com.xbot.kotogram.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.xbot.kotogram.core.designsystem.style.rememberStyle

// UIKit's and AppKit's glass button on iOS 26 and macOS 26 and later, for a bar over content; IconButton elsewhere.
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: IconButtonStyle = IconButtonStyle
) {
    val buttonStyle = rememberStyle(::IconButtonStyleScope, IconButtonStyle.Adaptive then style)
    with(LocalGlassIconButtonOverride.current) {
        GlassIconButtonOverrideScope(icon, contentDescription, onClick, modifier, enabled, buttonStyle)
            .GlassIconButton()
    }
}

internal interface GlassIconButtonOverride {
    @Composable
    fun GlassIconButtonOverrideScope.GlassIconButton()
}

internal class GlassIconButtonOverrideScope(
    val icon: ImageVector,
    val contentDescription: String?,
    val onClick: () -> Unit,
    val modifier: Modifier,
    val enabled: Boolean,
    val style: IconButtonStyleScope
) {
    val iconColor: Color get() = if (enabled) style.contentColor else style.disabledContentColor
}

internal object DefaultGlassIconButtonOverride : GlassIconButtonOverride {
    @Composable
    override fun GlassIconButtonOverrideScope.GlassIconButton() {
        context(style) {
            StyledIconButton(icon, contentDescription, onClick, modifier, enabled)
        }
    }
}

internal val LocalGlassIconButtonOverride: ProvidableCompositionLocal<GlassIconButtonOverride> =
    staticCompositionLocalOf { DefaultGlassIconButtonOverride }

package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.foundation.layout.size
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.sun.jna.Pointer
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.GlassIconButtonOverride
import com.xbot.kotogram.core.designsystem.component.GlassIconButtonOverrideScope
import com.xbot.kotogram.core.designsystem.extensions.isDark
import com.xbot.kotogram.core.designsystem.extensions.toPng
import com.xbot.kotogram.core.designsystem.native.MacGlassIconButton
import dev.nucleusframework.window.tao.NativeView
import dev.nucleusframework.window.tao.nucleusNsPlatformView

internal object MacGlassIconButtonOverride : GlassIconButtonOverride {
    @Composable
    override fun GlassIconButtonOverrideScope.GlassIconButton() {
        val currentOnClick by rememberUpdatedState(onClick)
        val currentEnabled by rememberUpdatedState(enabled)
        val button = remember { MacGlassIconButton { if (currentEnabled) currentOnClick() } }
        // AppKit tints template images only on borderless buttons, so the icon comes in its colour.
        val bitmap = rememberIconBitmap(icon, style.iconSize, iconColor)
        val png = remember(bitmap) { bitmap.toPng() }
        val points = style.iconSize.value.toDouble()
        val dark = KotogramTheme.colors.background.isDark()
        val inFront = isSettledInFront()
        NativeView(
            factory = {
                nucleusNsPlatformView(
                    handle = { Pointer.nativeValue(button.handle) },
                    onDispose = button::release
                )
            },
            modifier = modifier.minimumInteractiveComponentSize().size(style.size),
            update = {
                png?.let { button.setImage(it, points) }
                button.setAccessibilityLabel(contentDescription)
                button.setDark(dark)
                button.setEnabled(enabled)
                button.setVisible(inFront)
            },
            placedAsOverlay = true
        )
    }
}

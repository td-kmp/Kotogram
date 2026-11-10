package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.foundation.layout.size
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.appkit.MacGlassIconButton
import com.xbot.kotogram.core.designsystem.component.GlassIconButtonOverride
import com.xbot.kotogram.core.designsystem.component.GlassIconButtonOverrideScope
import com.xbot.kotogram.core.designsystem.extensions.isDark
import com.xbot.kotogram.core.designsystem.extensions.toPng
import dev.nucleusframework.window.tao.NativeView
import dev.nucleusframework.window.tao.nucleusNsPlatformView

internal object MacGlassIconButtonOverride : GlassIconButtonOverride {
    @Composable
    override fun GlassIconButtonOverrideScope.GlassIconButton() {
        val currentOnClick by rememberUpdatedState(onClick)
        val currentEnabled by rememberUpdatedState(enabled)
        val button = remember { MacGlassIconButton() }
        LaunchedEffect(button) {
            button.clicks().collect { if (currentEnabled) currentOnClick() }
        }
        // Workaround: AppKit tints template images only on borderless buttons, so the icon comes in its colour.
        val bitmap = rememberIconBitmap(icon, style.iconSize, iconColor)
        val png = remember(bitmap) { bitmap.toPng() }
        val points = style.iconSize.value.toDouble()
        LaunchedEffect(button, png, points) {
            png?.let { button.setImage(it, points) }
        }
        val dark = KotogramTheme.colors.background.isDark()
        NativeView(
            factory = { nucleusNsPlatformView(handle = { button.viewHandle }, onDispose = button::close) },
            modifier = modifier.minimumInteractiveComponentSize().size(style.size),
            update = {
                button.setAccessibilityLabel(contentDescription)
                button.setDark(dark)
                button.setEnabled(enabled)
            },
            placedAsOverlay = true,
        )
    }
}

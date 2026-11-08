package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.sun.jna.Pointer
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.ProgressIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.ProgressIndicatorOverrideScope
import com.xbot.kotogram.core.designsystem.native.MacProgressIndicator
import dev.nucleusframework.window.tao.NativeView
import dev.nucleusframework.window.tao.nucleusNsPlatformView

internal object MacSpinnerOverride : ProgressIndicatorOverride {
    @Composable
    override fun ProgressIndicatorOverrideScope.ProgressIndicator() {
        val indicator = remember { MacProgressIndicator() }
        val density = LocalDensity.current
        var large by remember { mutableStateOf(false) }
        val inFront = isSettledInFront()
        val background = KotogramTheme.colors.background
        NativeView(
            factory = {
                nucleusNsPlatformView(
                    handle = { Pointer.nativeValue(indicator.handle) },
                    onDispose = indicator::release
                )
            },
            modifier = modifier.onSizeChanged { large = with(density) { it.height.toDp() } >= LargeSize },
            update = {
                indicator.setColor(style.color, background)
                indicator.setLarge(large)
                indicator.setVisible(inFront)
                indicator.setAnimating(true)
            },
            placedAsOverlay = true
        )
    }
}

private val LargeSize = 32.dp

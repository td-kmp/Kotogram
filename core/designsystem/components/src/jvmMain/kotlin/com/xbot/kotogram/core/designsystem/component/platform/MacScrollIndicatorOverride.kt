package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.appkit.MacScroller
import com.xbot.kotogram.core.designsystem.component.ScrollIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.ScrollIndicatorOverrideScope
import com.xbot.kotogram.core.designsystem.extensions.isDark
import dev.nucleusframework.window.tao.NativeView
import dev.nucleusframework.window.tao.nucleusNsPlatformView
import kotlinx.coroutines.launch

internal object MacScrollIndicatorOverride : ScrollIndicatorOverride {
    @Composable
    override fun ScrollIndicatorOverrideScope.ScrollIndicator() {
        val scroller = remember { MacScroller() }
        val width = remember(scroller) { scroller.width.dp }
        val dark = KotogramTheme.colors.surface.isDark()
        LaunchedEffect(scroller, state) {
            launch {
                snapshotFlow { state.positionFraction to state.sizeFraction }.collect { (position, size) ->
                    scroller.update(position.toDouble(), size.toDouble())
                }
            }
            scroller.positions().collect { state.scrollToFraction(it.toFloat()) }
        }
        NativeView(
            factory = { nucleusNsPlatformView(handle = { scroller.viewHandle }, onDispose = scroller::close) },
            modifier = modifier.width(width),
            update = { scroller.isDark = dark },
            placedAsOverlay = true,
        )
    }
}

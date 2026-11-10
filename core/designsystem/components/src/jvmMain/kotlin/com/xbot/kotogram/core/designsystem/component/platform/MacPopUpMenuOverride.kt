package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import com.xbot.kotogram.core.designsystem.appkit.MacRect
import com.xbot.kotogram.core.designsystem.appkit.MacWindow
import com.xbot.kotogram.core.designsystem.component.DefaultMenuOverride
import com.xbot.kotogram.core.designsystem.component.MenuOverride
import com.xbot.kotogram.core.designsystem.component.MenuOverrideScope
import dev.nucleusframework.window.tao.LocalTaoWindow

internal object MacPopUpMenuOverride : MenuOverride {
    @Composable
    override fun MenuOverrideScope.Menu() {
        val window = LocalTaoWindow.current?.nsWindowHandle
        if (window == null) {
            with(DefaultMenuOverride) { Menu() }
            return
        }
        val density = LocalDensity.current.density
        var anchor by remember { mutableStateOf(Rect.Zero) }
        val currentOnSelect by rememberUpdatedState(onSelect)
        val currentOnDismiss by rememberUpdatedState(onDismissRequest)
        Box(
            modifier.onGloballyPositioned { coordinates ->
                coordinates.parentLayoutCoordinates?.let { anchor = it.boundsInWindow() }
            },
        )
        LaunchedEffect(expanded) {
            if (!expanded) return@LaunchedEffect
            val points = MacRect(
                left = (anchor.left / density).toDouble(),
                top = (anchor.top / density).toDouble(),
                right = (anchor.right / density).toDouble(),
                bottom = (anchor.bottom / density).toDouble(),
            )
            val chosen = MacWindow(window).use { it.popUpMenu(titles, selectedIndex, points) }
            chosen?.let(currentOnSelect)
            currentOnDismiss()
        }
    }
}

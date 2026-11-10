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
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import com.sun.jna.Pointer
import com.xbot.kotogram.core.designsystem.component.DefaultMenuOverride
import com.xbot.kotogram.core.designsystem.component.MenuOverride
import com.xbot.kotogram.core.designsystem.component.MenuOverrideScope
import com.xbot.kotogram.core.designsystem.native.MacMenu
import dev.nucleusframework.window.tao.LocalTaoWindow

internal object MacPopUpMenuOverride : MenuOverride {
    @Composable
    override fun MenuOverrideScope.Menu() {
        val window = LocalTaoWindow.current?.nsWindowHandle
        if (window == null) {
            with(DefaultMenuOverride) { Menu() }
            return
        }
        val density = LocalDensity.current
        var anchor by remember { mutableStateOf(Rect.Zero) }
        var rootHeight by remember { mutableStateOf(0f) }
        val currentOnSelect by rememberUpdatedState(onSelect)
        val currentOnDismiss by rememberUpdatedState(onDismissRequest)
        Box(
            modifier.onGloballyPositioned { coordinates ->
                coordinates.parentLayoutCoordinates?.let { anchor = it.boundsInWindow() }
                rootHeight = coordinates.findRootCoordinates().size.height.toFloat()
            },
        )
        LaunchedEffect(expanded) {
            if (!expanded) return@LaunchedEffect
            val chosen = MacMenu.show(
                window = Pointer(window),
                titles = titles,
                selectedIndex = selectedIndex,
                anchor = Rect(anchor.topLeft / density.density, anchor.bottomRight / density.density),
                height = rootHeight / density.density,
            )
            chosen?.let(currentOnSelect)
            currentOnDismiss()
        }
    }
}

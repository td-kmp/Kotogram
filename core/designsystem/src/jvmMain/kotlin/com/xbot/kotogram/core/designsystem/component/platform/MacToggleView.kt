package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.sun.jna.Pointer
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.extensions.isDark
import com.xbot.kotogram.core.designsystem.native.MacToggle
import dev.nucleusframework.window.tao.NativeView
import dev.nucleusframework.window.tao.nucleusNsPlatformView

@Composable
internal fun MacToggleView(
    create: ((Boolean) -> Unit) -> MacToggle,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean,
    modifier: Modifier,
) {
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
    var toggles by remember { mutableIntStateOf(0) }
    val toggle = remember {
        create { on ->
            currentOnCheckedChange?.invoke(on)
            toggles++
        }.also { it.setOn(checked, animated = false) }
    }
    val size = remember(toggle) { toggle.fittingSize.let { DpSize(it.width.dp, it.height.dp) } }
    val dark = KotogramTheme.colors.background.isDark()
    NativeView(
        factory = {
            nucleusNsPlatformView(
                handle = { Pointer.nativeValue(toggle.handle) },
                onDispose = toggle::release,
            )
        },
        modifier = modifier.size(size),
        update = {
            toggle.setEnabled(enabled)
            toggle.setDark(dark)
        },
        placedAsOverlay = true,
    )
    // Hack: every toggle recomposes this, so a change the caller doesn't take goes back.
    LaunchedEffect(checked, toggles) {
        if (toggle.isOn != checked) toggle.setOn(checked, animated = true)
    }
}

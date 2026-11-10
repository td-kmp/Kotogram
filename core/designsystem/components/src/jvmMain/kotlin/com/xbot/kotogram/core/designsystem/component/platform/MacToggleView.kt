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
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.appkit.MacToggle
import com.xbot.kotogram.core.designsystem.appkit.MacToggleKind
import com.xbot.kotogram.core.designsystem.extensions.isDark
import dev.nucleusframework.window.tao.NativeView
import dev.nucleusframework.window.tao.nucleusNsPlatformView

@Composable
internal fun MacToggleView(
    kind: MacToggleKind,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
    var toggles by remember { mutableIntStateOf(0) }
    val toggle = remember { MacToggle(kind).also { it.setOn(checked, animated = false) } }
    LaunchedEffect(toggle) {
        toggle.changes().collect { on ->
            currentOnCheckedChange?.invoke(on)
            toggles++
        }
    }
    val size = remember(toggle) { DpSize(toggle.fittingWidth.dp, toggle.fittingHeight.dp) }
    val dark = KotogramTheme.colors.background.isDark()
    NativeView(
        factory = { nucleusNsPlatformView(handle = { toggle.viewHandle }, onDispose = toggle::close) },
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

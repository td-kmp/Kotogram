package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.sun.jna.Pointer
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.extensions.appleWeight
import com.xbot.kotogram.core.designsystem.extensions.containerColor
import com.xbot.kotogram.core.designsystem.extensions.contentColor
import com.xbot.kotogram.core.designsystem.extensions.isDark
import dev.nucleusframework.window.tao.LocalTaoWindow
import dev.nucleusframework.window.tao.NativeView
import dev.nucleusframework.window.tao.nucleusNsPlatformView

@Composable
internal actual fun PlatformProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    state: ButtonState,
    colors: ButtonColors,
    contentPadding: PaddingValues
) {
    if (LocalTaoWindow.current != null && MacGlassButton.isSupported) {
        GlassProminentButton(text, onClick, modifier, state, colors, contentPadding)
    } else {
        FilledProminentButton(text, onClick, modifier, state, colors, contentPadding)
    }
}

@Composable
private fun GlassProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    state: ButtonState,
    colors: ButtonColors,
    contentPadding: PaddingValues
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val currentState by rememberUpdatedState(state)
    val button = remember { MacGlassButton { if (currentState == ButtonState.Enabled) currentOnClick() } }
    val style = KotogramTheme.typography.labelLarge
    val fontSize = with(LocalDensity.current) { style.fontSize.toDp().value.toDouble() }
    val dark = KotogramTheme.colors.background.isDark()
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val inFront = lifecycle.isAtLeast(Lifecycle.State.RESUMED)
    NativeProminentButtonBox(text, colors, contentPadding, modifier) {
        NativeView(
            factory = {
                nucleusNsPlatformView(
                    handle = { Pointer.nativeValue(button.handle) },
                    onDispose = button::release
                )
            },
            modifier = Modifier.matchParentSize(),
            update = {
                val loading = state == ButtonState.Loading
                button.setTitle(
                    text = if (loading) "" else text,
                    fontSize = fontSize,
                    fontWeight = style.fontWeight.appleWeight(),
                    color = colors.contentColor(state)
                )
                button.setBezelColor(colors.containerColor(state))
                button.setDark(dark)
                button.setLoading(loading)
                button.setEnabled(state != ButtonState.Disabled)
                button.setVisible(inFront)
            },
            placedAsOverlay = true
        )
    }
}

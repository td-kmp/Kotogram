package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import com.sun.jna.Pointer
import com.xbot.kotogram.core.designsystem.KotogramTheme
import dev.nucleusframework.window.tao.LocalTaoWindow
import dev.nucleusframework.window.tao.NativeView
import dev.nucleusframework.window.tao.nucleusNsPlatformView

@Composable
internal actual fun PlatformProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    state: ButtonState
) {
    if (LocalTaoWindow.current != null && MacGlassButton.isSupported) {
        GlassProminentButton(text, onClick, modifier, state)
    } else {
        FilledProminentButton(text, onClick, modifier, state)
    }
}

@Composable
private fun GlassProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    state: ButtonState
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val currentState by rememberUpdatedState(state)
    val button = remember { MacGlassButton { if (currentState == ButtonState.Enabled) currentOnClick() } }
    val size = remember(text) { button.sizeFor(text) }
    val tint = KotogramTheme.colors.primary
    val dark = KotogramTheme.colors.background.luminance() < HALF_LUMINANCE
    val loading = state == ButtonState.Loading
    Box(modifier.defaultMinSize(size.width, size.height), contentAlignment = Alignment.Center) {
        NativeView(
            factory = {
                nucleusNsPlatformView(
                    handle = { Pointer.nativeValue(button.handle) },
                    onDispose = button::release
                )
            },
            modifier = Modifier.matchParentSize(),
            update = {
                button.update(
                    title = if (loading) "" else text,
                    enabled = state != ButtonState.Disabled,
                    tint = tint,
                    dark = dark
                )
            }
        )
        // Compose draws over the native view, so the spinner can be ours.
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(ButtonDefaults.ProgressSize),
                color = KotogramTheme.colors.onPrimary,
                strokeWidth = ButtonDefaults.ProgressStrokeWidth
            )
        }
    }
}

private const val HALF_LUMINANCE = 0.5f

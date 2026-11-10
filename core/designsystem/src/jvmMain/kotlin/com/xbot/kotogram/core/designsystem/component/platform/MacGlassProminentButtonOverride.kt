package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.sun.jna.Pointer
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.NativeBox
import com.xbot.kotogram.core.designsystem.component.ProminentButtonOverride
import com.xbot.kotogram.core.designsystem.component.ProminentButtonOverrideScope
import com.xbot.kotogram.core.designsystem.extensions.appleWeight
import com.xbot.kotogram.core.designsystem.extensions.containerColor
import com.xbot.kotogram.core.designsystem.extensions.contentColor
import com.xbot.kotogram.core.designsystem.extensions.isDark
import com.xbot.kotogram.core.designsystem.native.MacGlassButton
import dev.nucleusframework.window.tao.NativeView
import dev.nucleusframework.window.tao.nucleusNsPlatformView

internal object MacGlassProminentButtonOverride : ProminentButtonOverride {
    @Composable
    override fun ProminentButtonOverrideScope.ProminentButton() {
        val currentOnClick by rememberUpdatedState(onClick)
        val currentState by rememberUpdatedState(state)
        val button = remember { MacGlassButton { if (currentState == ButtonState.Enabled) currentOnClick() } }
        val textStyle = buttonStyle.textStyle
        val fontSize = with(LocalDensity.current) { textStyle.fontSize.toDp().value.toDouble() }
        val dark = KotogramTheme.colors.background.isDark()
        NativeBox {
            NativeView(
                factory = {
                    nucleusNsPlatformView(
                        handle = { Pointer.nativeValue(button.handle) },
                        onDispose = button::release,
                    )
                },
                modifier = Modifier.matchParentSize(),
                update = {
                    val loading = state == ButtonState.Loading
                    button.setTitle(
                        text = if (loading) "" else text,
                        fontSize = fontSize,
                        fontWeight = textStyle.fontWeight.appleWeight(),
                        color = colors.contentColor(state),
                    )
                    button.setBezelColor(colors.containerColor(state))
                    button.setDark(dark)
                    button.setLoading(loading, colors.contentColor(state), colors.containerColor(state))
                    button.setEnabled(state != ButtonState.Disabled)
                },
                placedAsOverlay = true,
            )
        }
    }
}

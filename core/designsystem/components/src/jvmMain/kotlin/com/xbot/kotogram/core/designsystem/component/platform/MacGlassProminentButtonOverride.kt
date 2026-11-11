package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.appkit.MacGlassButton
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.NativeBox
import com.xbot.kotogram.core.designsystem.component.ProminentButtonOverride
import com.xbot.kotogram.core.designsystem.component.ProminentButtonOverrideScope
import com.xbot.kotogram.core.designsystem.extensions.appleWeight
import com.xbot.kotogram.core.designsystem.extensions.containerColor
import com.xbot.kotogram.core.designsystem.extensions.contentColor
import com.xbot.kotogram.core.designsystem.extensions.isDark
import com.xbot.kotogram.core.designsystem.extensions.isLightOn
import dev.nucleusframework.window.tao.NativeView
import dev.nucleusframework.window.tao.nucleusNsPlatformView

internal object MacGlassProminentButtonOverride : ProminentButtonOverride {
    @Composable
    override fun ProminentButtonOverrideScope.ProminentButton() {
        val currentOnClick by rememberUpdatedState(onClick)
        val currentState by rememberUpdatedState(state)
        val button = remember { MacGlassButton() }
        LaunchedEffect(button) {
            button.clicks().collect { if (currentState == ButtonState.Enabled) currentOnClick() }
        }
        val textStyle = buttonStyle.textStyle
        val fontSize = with(LocalDensity.current) { textStyle.fontSize.toDp().value.toDouble() }
        val dark = KotogramTheme.colors.surface.isDark()
        NativeBox {
            NativeView(
                factory = { nucleusNsPlatformView(handle = { button.viewHandle }, onDispose = button::close) },
                modifier = Modifier.matchParentSize(),
                update = {
                    val loading = state == ButtonState.Loading
                    val contentColor = colors.contentColor(state)
                    val containerColor = colors.containerColor(state)
                    button.setTitle(
                        text = if (loading) "" else text,
                        fontSize = fontSize,
                        fontWeight = textStyle.fontWeight.appleWeight(),
                        argb = contentColor.toArgb(),
                    )
                    button.bezelColor = containerColor.toArgb()
                    button.isDark = dark
                    button.setLoading(loading, lightSpinner = contentColor.isLightOn(containerColor))
                    button.isEnabled = state != ButtonState.Disabled
                },
                placedAsOverlay = true,
            )
        }
    }
}

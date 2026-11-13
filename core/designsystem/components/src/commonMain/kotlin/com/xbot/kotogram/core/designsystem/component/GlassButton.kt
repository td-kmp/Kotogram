package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import com.xbot.kotogram.core.designsystem.style.rememberStyle

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ButtonState = ButtonState.Enabled,
    style: ButtonStyle = ButtonStyle,
) {
    val buttonStyle = rememberStyle(::ButtonStyleScope, ButtonStyle.Adaptive then style)
    with(LocalGlassButtonOverride.current) {
        GlassButtonOverrideScope(text, onClick, modifier, state, buttonStyle).GlassButton()
    }
}

internal interface GlassButtonOverride {
    @Composable
    fun GlassButtonOverrideScope.GlassButton()
}

internal class GlassButtonOverrideScope(
    val text: String,
    val onClick: () -> Unit,
    val modifier: Modifier,
    val state: ButtonState,
    val buttonStyle: ButtonStyleScope,
) {
    val colors: ButtonColors get() = buttonStyle.buttonColors()
}

internal object DefaultGlassButtonOverride : GlassButtonOverride {
    @Composable
    override fun GlassButtonOverrideScope.GlassButton() {
        context(buttonStyle) {
            StyledButton(onClick = onClick, modifier = modifier, state = state, interactionSource = null) {
                Text(text)
            }
        }
    }
}

internal val LocalGlassButtonOverride: ProvidableCompositionLocal<GlassButtonOverride> =
    staticCompositionLocalOf { DefaultGlassButtonOverride }

@Composable
internal fun GlassButtonOverrideScope.NativeBox(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier.minimumInteractiveComponentSize(),
        contentAlignment = Alignment.Center,
        propagateMinConstraints = true,
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            with(DefaultGlassButtonOverride) {
                GlassButtonOverrideScope(
                    text = text,
                    onClick = {},
                    modifier = Modifier.alpha(0f).clearAndSetSemantics {},
                    state = ButtonState.Disabled,
                    buttonStyle = buttonStyle,
                ).GlassButton()
            }
        }
        content()
    }
}

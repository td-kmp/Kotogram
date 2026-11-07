package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
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

@Composable
fun ProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ButtonState = ButtonState.Enabled,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    with(LocalProminentButtonOverride.current) {
        ProminentButtonOverrideScope(text, onClick, modifier, state, colors, contentPadding).ProminentButton()
    }
}

internal interface ProminentButtonOverride {
    @Composable
    fun ProminentButtonOverrideScope.ProminentButton()
}

internal class ProminentButtonOverrideScope(
    val text: String,
    val onClick: () -> Unit,
    val modifier: Modifier,
    val state: ButtonState,
    val colors: ButtonColors,
    val contentPadding: PaddingValues
)

internal object DefaultProminentButtonOverride : ProminentButtonOverride {
    @Composable
    override fun ProminentButtonOverrideScope.ProminentButton() {
        Button(
            onClick = onClick,
            modifier = modifier,
            state = state,
            colors = colors,
            contentPadding = contentPadding
        ) {
            Text(text)
        }
    }
}

internal val LocalProminentButtonOverride: ProvidableCompositionLocal<ProminentButtonOverride> =
    staticCompositionLocalOf { DefaultProminentButtonOverride }

@Composable
internal fun ProminentButtonOverrideScope.NativeBox(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier.minimumInteractiveComponentSize(),
        contentAlignment = Alignment.Center,
        propagateMinConstraints = true
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            with(DefaultProminentButtonOverride) {
                ProminentButtonOverrideScope(
                    text = text,
                    onClick = {},
                    modifier = Modifier.alpha(0f).clearAndSetSemantics {},
                    state = ButtonState.Disabled,
                    colors = colors,
                    contentPadding = contentPadding
                ).ProminentButton()
            }
        }
        content()
    }
}

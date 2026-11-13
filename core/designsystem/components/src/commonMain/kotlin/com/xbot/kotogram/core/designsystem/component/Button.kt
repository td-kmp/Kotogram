package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.PreviewKotogram
import com.xbot.kotogram.core.designsystem.PreviewKotogramLaptop
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.designsystem.theme.currentPlatform
import androidx.compose.material3.Button as MaterialButton

enum class ButtonState {
    Enabled,
    Disabled,
    Loading,
    ;

    companion object {
        fun of(
            enabled: Boolean,
            loading: Boolean = false,
        ): ButtonState = when {
            loading -> Loading
            enabled -> Enabled
            else -> Disabled
        }
    }
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ButtonState = ButtonState.Enabled,
    style: ButtonStyle = ButtonStyle,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    context(rememberStyle(::ButtonStyleScope, ButtonStyle.Adaptive then style)) {
        StyledButton(onClick, state, interactionSource, modifier, content)
    }
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ButtonState = ButtonState.Enabled,
    style: TextButtonStyle = TextButtonStyle,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    context(rememberStyle(::ButtonStyleScope, TextButtonStyle.Adaptive then style)) {
        StyledButton(onClick, state, interactionSource, modifier, content)
    }
}

@Composable
context(style: ButtonStyleScope)
internal fun StyledButton(
    onClick: () -> Unit,
    state: ButtonState,
    interactionSource: MutableInteractionSource?,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val ripple = LocalRippleConfiguration.current.takeIf { style.ripple }
    CompositionLocalProvider(LocalRippleConfiguration provides ripple) {
        MaterialButton(
            onClick = onClick,
            modifier = modifier.then(pressFeedback(source)).heightIn(min = style.minHeight),
            enabled = state == Enabled,
            shape = style.shape,
            colors = style.buttonColors().during(state),
            elevation = null,
            contentPadding = style.contentPadding,
            interactionSource = source,
        ) {
            CompositionLocalProvider(LocalTextStyle provides style.textStyle) {
                ButtonContent(state, content)
            }
        }
    }
}

@Composable
private fun pressFeedback(source: MutableInteractionSource): Modifier =
    if (currentPlatform.isApple) Modifier.indication(source, KotogramTheme.indication) else Modifier

private fun ButtonColors.during(state: ButtonState): ButtonColors = if (state == Loading) {
    copy(disabledContainerColor = containerColor, disabledContentColor = contentColor)
} else {
    this
}

@Composable
context(style: ButtonStyleScope)
private fun ButtonContent(
    state: ButtonState,
    content: @Composable RowScope.() -> Unit,
) {
    Box(contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier.alpha(if (state == Loading) 0f else 1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
        if (state == Loading) {
            val contentColor = LocalContentColor.current
            ProgressIndicator(
                modifier = Modifier.size(style.progressSize),
                style = remember(contentColor) { ProgressIndicatorStyle { color(contentColor) } },
            )
        }
    }
}

@PreviewKotogram
@Composable
private fun ButtonPreview() {
    Column(Modifier.padding(KotogramTheme.spacing.large)) {
        ButtonState.entries.forEach { state ->
            Row(horizontalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.small)) {
                Button(onClick = {}, state = state) { Text("Next") }
                TextButton(onClick = {}, state = state) { Text("Forgot password?") }
            }
        }
    }
}

@PreviewKotogramLaptop
@Composable
private fun ButtonLaptopPreview() = ButtonPreview()

@Suppress("MagicNumber")
fun interface ButtonStyle : ComponentStyle<ButtonStyleScope> {
    companion object : ButtonStyle {
        override fun ButtonStyleScope.applyStyle() {}

        val Default = ButtonStyle {
            minHeight(50.dp)
            shape(CircleShape)
            contentPadding(PaddingValues(horizontal = 24.dp, vertical = 6.dp))
            containerColor(theme.colors.primary)
            contentColor(theme.colors.onPrimary)
            disabledContainerColor(theme.colors.onSurface.copy(alpha = 0.1f))
            disabledContentColor(theme.colors.onSurfaceVariant.copy(alpha = 0.38f))
            textStyle(theme.typography.labelLarge)
            progressSize(22.dp)
            ripple(true)
        }

        val Adaptive = Default then ButtonStyle {
            laptop {
                minHeight(36.dp)
                contentPadding(PaddingValues(horizontal = 15.dp, vertical = 6.dp))
                progressSize(18.dp)
            }
        }
    }
}

infix fun ButtonStyle.then(other: ButtonStyle): ButtonStyle = when {
    other === ButtonStyle -> this

    this === ButtonStyle -> other

    else -> ButtonStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("MagicNumber")
fun interface TextButtonStyle : ComponentStyle<ButtonStyleScope> {
    companion object : TextButtonStyle {
        override fun ButtonStyleScope.applyStyle() {}

        val Default = TextButtonStyle {
            minHeight(50.dp)
            shape(CircleShape)
            contentPadding(PaddingValues(horizontal = 12.dp, vertical = 6.dp))
            containerColor(Color.Transparent)
            contentColor(theme.colors.primary)
            disabledContainerColor(Color.Transparent)
            disabledContentColor(theme.colors.onSurfaceVariant.copy(alpha = 0.38f))
            textStyle(theme.typography.bodyLarge)
            progressSize(22.dp)
            ripple(true)
        }

        val Adaptive = Default then TextButtonStyle {
            laptop {
                minHeight(36.dp)
                contentPadding(PaddingValues(horizontal = 10.dp, vertical = 6.dp))
                progressSize(18.dp)
            }
        }
    }
}

infix fun TextButtonStyle.then(other: TextButtonStyle): TextButtonStyle = when {
    other === TextButtonStyle -> this

    this === TextButtonStyle -> other

    else -> TextButtonStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

class ButtonStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<ButtonStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var minHeight: Dp = Dp.Unspecified
        private set

    var shape: Shape = RectangleShape
        private set

    var contentPadding: PaddingValues = PaddingValues()
        private set

    var containerColor: Color = Color.Unspecified
        private set

    var contentColor: Color = Color.Unspecified
        private set

    var disabledContainerColor: Color = Color.Unspecified
        private set

    var disabledContentColor: Color = Color.Unspecified
        private set

    var textStyle: TextStyle = TextStyle.Default
        private set

    var progressSize: Dp = Dp.Unspecified
        private set

    var ripple: Boolean = true
        private set

    fun minHeight(height: Dp) {
        minHeight = height
    }

    fun shape(shape: Shape) {
        this.shape = shape
    }

    fun contentPadding(padding: PaddingValues) {
        contentPadding = padding
    }

    fun containerColor(color: Color) {
        containerColor = color
    }

    fun contentColor(color: Color) {
        contentColor = color
    }

    fun disabledContainerColor(color: Color) {
        disabledContainerColor = color
    }

    fun disabledContentColor(color: Color) {
        disabledContentColor = color
    }

    fun textStyle(style: TextStyle) {
        textStyle = style
    }

    fun progressSize(size: Dp) {
        progressSize = size
    }

    fun ripple(enabled: Boolean) {
        ripple = enabled
    }

    internal fun buttonColors(): ButtonColors =
        ButtonColors(containerColor, contentColor, disabledContainerColor, disabledContentColor)
}

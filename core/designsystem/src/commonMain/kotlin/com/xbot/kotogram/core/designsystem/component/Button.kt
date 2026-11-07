package com.xbot.kotogram.core.designsystem.component

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import androidx.compose.material3.Button as MaterialButton
import androidx.compose.material3.ButtonDefaults as MaterialButtonDefaults
import androidx.compose.material3.TextButton as MaterialTextButton

enum class ButtonState {
    Enabled,
    Disabled,
    Loading;

    companion object {
        fun of(enabled: Boolean, loading: Boolean = false): ButtonState = when {
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
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    MaterialButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonDefaults.MinHeight),
        enabled = state == ButtonState.Enabled,
        shape = shape,
        colors = colors.during(state),
        contentPadding = contentPadding,
        interactionSource = interactionSource
    ) {
        ButtonContent(state, content)
    }
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ButtonState = ButtonState.Enabled,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    MaterialTextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonDefaults.MinHeight),
        enabled = state == ButtonState.Enabled,
        shape = shape,
        colors = colors.during(state),
        contentPadding = contentPadding,
        interactionSource = interactionSource
    ) {
        // Telegram for macOS's text buttons are links in the regular weight, not Material's medium labels.
        CompositionLocalProvider(LocalTextStyle provides KotogramTheme.typography.bodyLarge) {
            ButtonContent(state, content)
        }
    }
}

object ButtonDefaults {
    val MinHeight: Dp = 36.dp

    val ContentPadding: PaddingValues = PaddingValues(horizontal = 15.dp, vertical = 6.dp)

    val TextButtonContentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 6.dp)

    val ProgressSize: Dp = 18.dp

    val ProgressStrokeWidth: Dp = 2.dp

    val shape: Shape = CircleShape

    @Composable
    fun buttonColors(): ButtonColors = MaterialButtonDefaults.buttonColors()

    @Composable
    fun textButtonColors(): ButtonColors = MaterialButtonDefaults.textButtonColors()
}

// A loading button keeps its colours and its width: the spinner takes the place of the content.
private fun ButtonColors.during(state: ButtonState): ButtonColors = if (state == ButtonState.Loading) {
    copy(disabledContainerColor = containerColor, disabledContentColor = contentColor)
} else {
    this
}

@Composable
private fun ButtonContent(state: ButtonState, content: @Composable RowScope.() -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier.alpha(if (state == ButtonState.Loading) 0f else 1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
        if (state == ButtonState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(ButtonDefaults.ProgressSize),
                color = LocalContentColor.current,
                strokeWidth = ButtonDefaults.ProgressStrokeWidth
            )
        }
    }
}

@KotogramPreview
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

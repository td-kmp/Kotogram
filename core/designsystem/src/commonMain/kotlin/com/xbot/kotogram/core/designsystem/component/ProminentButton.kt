package com.xbot.kotogram.core.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ButtonState = ButtonState.Enabled
) {
    PlatformProminentButton(text, onClick, modifier, state)
}

@Composable
internal fun FilledProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    state: ButtonState
) {
    Button(onClick = onClick, modifier = modifier, state = state) { Text(text) }
}

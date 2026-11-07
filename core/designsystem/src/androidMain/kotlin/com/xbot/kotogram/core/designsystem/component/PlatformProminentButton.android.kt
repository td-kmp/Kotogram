package com.xbot.kotogram.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun PlatformProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    state: ButtonState
) {
    FilledProminentButton(text, onClick, modifier, state)
}

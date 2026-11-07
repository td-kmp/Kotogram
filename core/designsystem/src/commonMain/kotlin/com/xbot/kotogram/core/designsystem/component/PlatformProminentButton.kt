package com.xbot.kotogram.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// UIKit's prominent glass button on iOS 26 and later, the filled Button everywhere else.
@Composable
internal expect fun PlatformProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    state: ButtonState
)

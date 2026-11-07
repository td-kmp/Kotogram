package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xbot.kotogram.core.designsystem.component.ButtonState

@Composable
internal expect fun PlatformProminentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    state: ButtonState,
    colors: ButtonColors,
    contentPadding: PaddingValues
)

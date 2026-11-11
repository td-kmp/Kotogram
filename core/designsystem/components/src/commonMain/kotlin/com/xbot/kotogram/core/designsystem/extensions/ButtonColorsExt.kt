package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.material3.ButtonColors
import androidx.compose.ui.graphics.Color
import com.xbot.kotogram.core.designsystem.component.ButtonState

internal fun ButtonColors.containerColor(state: ButtonState): Color =
    if (state == ButtonState.Disabled) disabledContainerColor else containerColor

internal fun ButtonColors.contentColor(state: ButtonState): Color =
    if (state == ButtonState.Disabled) disabledContentColor else contentColor

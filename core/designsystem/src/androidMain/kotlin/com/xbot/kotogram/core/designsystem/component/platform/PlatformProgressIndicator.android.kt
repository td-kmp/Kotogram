package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.xbot.kotogram.core.designsystem.component.MaterialProgressIndicator

@Composable
internal actual fun PlatformProgressIndicator(modifier: Modifier, color: Color) {
    MaterialProgressIndicator(modifier, color)
}

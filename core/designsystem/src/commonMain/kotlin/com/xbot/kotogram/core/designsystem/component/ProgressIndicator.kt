package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.platform.PlatformProgressIndicator

@Composable
fun ProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.color
) {
    PlatformProgressIndicator(modifier.size(ProgressIndicatorDefaults.Size), color)
}

@Composable
internal fun MaterialProgressIndicator(modifier: Modifier, color: Color) {
    CircularProgressIndicator(modifier = modifier, color = color, strokeWidth = ProgressIndicatorDefaults.StrokeWidth)
}

object ProgressIndicatorDefaults {
    val Size: Dp = 20.dp

    val StrokeWidth: Dp = 2.dp

    val color: Color
        @Composable get() = KotogramTheme.colors.primary
}

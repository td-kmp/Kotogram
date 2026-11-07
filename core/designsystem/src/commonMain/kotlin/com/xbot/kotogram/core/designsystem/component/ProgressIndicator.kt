package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme

@Composable
fun ProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.color
) {
    with(LocalProgressIndicatorOverride.current) {
        ProgressIndicatorOverrideScope(modifier.size(ProgressIndicatorDefaults.Size), color).ProgressIndicator()
    }
}

object ProgressIndicatorDefaults {
    val Size: Dp = 20.dp

    val StrokeWidth: Dp = 2.dp

    val color: Color
        @Composable get() = KotogramTheme.colors.primary
}

internal interface ProgressIndicatorOverride {
    @Composable
    fun ProgressIndicatorOverrideScope.ProgressIndicator()
}

internal class ProgressIndicatorOverrideScope(val modifier: Modifier, val color: Color)

internal object DefaultProgressIndicatorOverride : ProgressIndicatorOverride {
    @Composable
    override fun ProgressIndicatorOverrideScope.ProgressIndicator() {
        CircularProgressIndicator(
            modifier = modifier,
            color = color,
            strokeWidth = ProgressIndicatorDefaults.StrokeWidth
        )
    }
}

internal val LocalProgressIndicatorOverride: ProvidableCompositionLocal<ProgressIndicatorOverride> =
    staticCompositionLocalOf { DefaultProgressIndicatorOverride }

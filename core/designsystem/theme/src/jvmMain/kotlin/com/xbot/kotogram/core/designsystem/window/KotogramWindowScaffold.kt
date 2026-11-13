package com.xbot.kotogram.core.designsystem.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.nucleusframework.window.DecoratedWindowScope
import dev.nucleusframework.window.TitleBarPlacement
import dev.nucleusframework.window.WindowScaffold
import dev.nucleusframework.window.material.MaterialTitleBar
import dev.nucleusframework.window.styling.LocalTitleBarStyle

@Composable
fun DecoratedWindowScope.KotogramWindowScaffold(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val style = LocalTitleBarStyle.current
    WindowScaffold(
        modifier = modifier,
        titleBar = {
            MaterialTitleBar(
                style = style.copy(
                    colors = style.colors.copy(
                        background = Color.Transparent,
                        inactiveBackground = Color.Transparent,
                        border = Color.Transparent,
                    ),
                    metrics = style.metrics.copy(height = TitleBarHeight),
                ),
            )
        },
        titleBarPlacement = TitleBarPlacement.Overlay(autoHideInFullscreen = false, passThroughToContent = true),
    ) { content() }
}

private val TitleBarHeight = 52.dp

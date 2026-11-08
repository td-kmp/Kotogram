package com.xbot.kotogram.core.designsystem.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity

@Composable
internal actual fun <S> platformNavTransitions(): NavTransitions<S> {
    val density = LocalDensity.current
    return remember(density) { androidNavTransitions(density) }
}

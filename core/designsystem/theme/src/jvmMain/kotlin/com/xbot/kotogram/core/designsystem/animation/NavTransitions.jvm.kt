package com.xbot.kotogram.core.designsystem.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
internal actual fun <S> platformNavTransitions(): NavTransitions<S> {
    val layoutDirection = LocalLayoutDirection.current
    return remember(layoutDirection) { macNavTransitions(layoutDirection) }
}

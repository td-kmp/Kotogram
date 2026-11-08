package com.xbot.kotogram.core.designsystem.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun <S> platformNavTransitions(): NavTransitions<S> = remember {
    macNavTransitions()
}

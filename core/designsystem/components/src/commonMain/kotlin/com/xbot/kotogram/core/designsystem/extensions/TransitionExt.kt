package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.animation.core.Transition

internal val <T : Any> Transition<T?>.shown: T? get() = targetState ?: currentState

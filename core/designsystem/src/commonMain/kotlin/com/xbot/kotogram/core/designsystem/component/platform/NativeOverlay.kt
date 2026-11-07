package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

@Composable
internal fun isSettledInFront(): Boolean {
    val state by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    return state.isAtLeast(Lifecycle.State.RESUMED)
}

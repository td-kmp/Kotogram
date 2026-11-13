package com.xbot.kotogram.feature.connection.impl.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
actual fun isAppInForeground(): Boolean {
    val state by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    return state.isAtLeast(Lifecycle.State.RESUMED)
}

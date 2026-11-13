package com.xbot.kotogram.feature.connection.impl.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

@Composable
internal actual fun rememberAppForeground(): State<Boolean> {
    val state = LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    return remember(state) { derivedStateOf { state.value.isAtLeast(Lifecycle.State.RESUMED) } }
}

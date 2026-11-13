package com.xbot.kotogram.feature.connection.impl.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo

@Composable
internal actual fun rememberAppForeground(): State<Boolean> {
    val windowInfo = LocalWindowInfo.current
    return remember(windowInfo) { derivedStateOf { windowInfo.isWindowFocused } }
}

package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

@Immutable
internal class WindowChrome(
    val height: Int,
    val start: Int,
    val end: Int,
)

@Composable
internal expect fun windowChrome(): WindowChrome?

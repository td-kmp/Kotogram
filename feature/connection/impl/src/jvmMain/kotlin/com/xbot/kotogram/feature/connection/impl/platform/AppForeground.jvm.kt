package com.xbot.kotogram.feature.connection.impl.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalWindowInfo

@Composable
actual fun isAppInForeground(): Boolean = LocalWindowInfo.current.isWindowFocused

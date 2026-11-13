package com.xbot.kotogram.feature.connection.impl.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

@Composable
internal expect fun rememberAppForeground(): State<Boolean>

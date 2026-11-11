package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

internal fun Path.moveTo(point: Offset) = moveTo(point.x, point.y)

internal fun Path.lineTo(point: Offset) = lineTo(point.x, point.y)

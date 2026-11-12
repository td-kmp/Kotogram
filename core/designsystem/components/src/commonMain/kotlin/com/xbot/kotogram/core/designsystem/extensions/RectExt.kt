package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.geometry.Rect

internal fun Rect.encloses(other: Rect): Boolean =
    left <= other.left && top <= other.top && right >= other.right && bottom >= other.bottom

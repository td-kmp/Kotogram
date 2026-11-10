package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun Offset.Companion.polar(
    radius: Float,
    degrees: Float,
): Offset {
    val radians = degrees * PI / HALF_TURN
    return Offset((radius * cos(radians)).toFloat(), (radius * sin(radians)).toFloat())
}

private const val HALF_TURN = 180f

package com.xbot.kotogram.core.designsystem.icon

import androidx.compose.ui.geometry.Offset
import kotlin.math.hypot

// The part of a square canvas at least `distance` to the left of the line a → b.
internal fun halfPlane(a: Offset, b: Offset, distance: Float, canvas: Float): List<Offset> {
    val length = hypot(b.x - a.x, b.y - a.y)
    val normal = Offset(-(b.y - a.y) / length, (b.x - a.x) / length)
    val side = { point: Offset -> (point - a).x * normal.x + (point - a).y * normal.y - distance }
    val square = listOf(Offset(0f, 0f), Offset(canvas, 0f), Offset(canvas, canvas), Offset(0f, canvas))
    val kept = mutableListOf<Offset>()
    square.indices.forEach { index ->
        val current = square[index]
        val next = square[(index + 1) % square.size]
        val currentSide = side(current)
        val nextSide = side(next)
        if (currentSide >= 0) kept += current
        if ((currentSide >= 0) != (nextSide >= 0)) {
            kept += current + (next - current) * (currentSide / (currentSide - nextSide))
        }
    }
    return kept
}

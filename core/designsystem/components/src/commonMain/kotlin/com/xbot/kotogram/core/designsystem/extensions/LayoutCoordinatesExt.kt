package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.findRootCoordinates
import kotlin.math.abs

internal fun LayoutCoordinates.windowClipOf(bounds: Rect): Rect {
    val holder = generateSequence(this) { it.parentLayoutCoordinates }
        .firstOrNull { it.boundsInWindow(clipBounds = false).encloses(bounds) }
        ?: this
    return holder.boundsInWindow(clipBounds = true)
}

internal fun LayoutCoordinates.isOnlyTranslated(matrix: Matrix): Boolean {
    matrix.reset()
    findRootCoordinates().transformFrom(this, matrix)
    val values = matrix.values
    val unscaled = abs(values[Matrix.ScaleX] - 1f) < EPSILON && abs(values[Matrix.ScaleY] - 1f) < EPSILON
    val unturned = abs(values[Matrix.SkewX]) < EPSILON && abs(values[Matrix.SkewY]) < EPSILON
    val flat = values[Matrix.Perspective0] == 0f && values[Matrix.Perspective1] == 0f
    return unscaled && unturned && flat
}

private const val EPSILON = 1e-3f

@file:Suppress("MagicNumber")

package com.xbot.kotogram.core.designsystem.icon

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.group
import com.xbot.kotogram.core.designsystem.extensions.line
import com.xbot.kotogram.core.designsystem.extensions.polygon
import com.xbot.kotogram.core.designsystem.extensions.polyline
import com.xbot.kotogram.core.designsystem.extensions.roundRect
import com.xbot.kotogram.core.designsystem.extensions.stroke

internal fun replyIcon() = icon("Reply") {
    stroke(width = LINE) {
        polyline(listOf(Offset(9f, 5.5f), Offset(4.5f, 10f), Offset(9f, 14.5f)))
        moveTo(4.5f, 10f)
        lineTo(14f, 10f)
        arcTo(5.5f, 5.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19.5f, 15.5f)
        lineTo(19.5f, 19f)
    }
}

internal fun forwardIcon() = icon("Forward") {
    stroke(width = LINE) {
        polyline(listOf(Offset(15f, 5.5f), Offset(19.5f, 10f), Offset(15f, 14.5f)))
        moveTo(19.5f, 10f)
        lineTo(10f, 10f)
        arcTo(5.5f, 5.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.5f, 15.5f)
        lineTo(4.5f, 19f)
    }
}

internal fun copyIcon() = icon("Copy") {
    stroke(width = LINE) { roundRect(3.5f, 8.5f, 15.5f, 20.5f, 2.5f) }
    val canvas = listOf(Offset(0f, 0f), Offset(CANVAS, 0f), Offset(CANVAS, CANVAS), Offset(0f, CANVAS))
    val front = listOf(Offset(2f, 7f), Offset(2f, 22f), Offset(17f, 22f), Offset(17f, 7f))
    group(clipPathData = clip { polygon(canvas); polygon(front) }) {
        stroke(width = LINE) { roundRect(8.5f, 3.5f, 20.5f, 15.5f, 2.5f) }
    }
}

internal fun deleteIcon() = icon("Delete") {
    stroke(width = LINE) {
        line(Offset(4.5f, 6.5f), Offset(19.5f, 6.5f))
        polyline(listOf(Offset(9.5f, 6.5f), Offset(10.5f, 4f), Offset(13.5f, 4f), Offset(14.5f, 6.5f)))
        moveTo(6f, 6.5f)
        lineTo(7f, 18f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9f, 20f)
        lineTo(15f, 20f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 17f, 18f)
        lineTo(18f, 6.5f)
        line(Offset(10f, 10f), Offset(10f, 16.5f))
        line(Offset(14f, 10f), Offset(14f, 16.5f))
    }
}

internal fun arrowDownIcon() = icon("ArrowDown") {
    stroke(width = LINE) {
        line(Offset(12f, 5f), Offset(12f, 18.5f))
        polyline(listOf(Offset(6.5f, 13f), Offset(12f, 18.5f), Offset(17.5f, 13f)))
    }
}

@file:Suppress("MagicNumber")

package com.xbot.kotogram.core.designsystem.icon

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathFillType
import com.xbot.kotogram.core.designsystem.extensions.circle
import com.xbot.kotogram.core.designsystem.extensions.cross
import com.xbot.kotogram.core.designsystem.extensions.fill
import com.xbot.kotogram.core.designsystem.extensions.line
import com.xbot.kotogram.core.designsystem.extensions.polyline
import com.xbot.kotogram.core.designsystem.extensions.stroke

internal fun backIcon() = icon("Back") {
    stroke(width = 2f) { polyline(listOf(Offset(15f, 6f), Offset(9f, 12f), Offset(15f, 18f))) }
}

internal fun closeIcon() = icon("Close") {
    stroke(width = LINE) {
        circle(Center, 10f)
        line(Offset(8f, 8f), Offset(16f, 16f))
        line(Offset(8f, 16f), Offset(16f, 8f))
    }
}

internal fun crossIcon() = icon("Cross") {
    stroke(width = 2f) {
        line(Offset(7f, 7f), Offset(17f, 17f))
        line(Offset(7f, 17f), Offset(17f, 7f))
    }
}

internal fun clearIcon() = icon("Clear") {
    fill(PathFillType.EvenOdd) {
        circle(Center, 6.5f)
        cross(Center, halfExtent = 2.1f, halfWidth = 0.685f)
    }
}

internal fun searchIcon() = icon("Search") {
    stroke(width = 1f) { circle(Offset(11f, 11f), 4.5f) }
    stroke(width = 1.07f) { line(Offset(14.18f, 14.18f), Offset(17.4f, 17.4f)) }
}

internal fun chevronDownIcon() = icon("ChevronDown") {
    stroke(width = 1.5f) { polyline(listOf(Offset(8f, 10f), Offset(12f, 14f), Offset(16f, 10f))) }
}

internal fun chevronRightIcon() = icon("ChevronRight") {
    stroke(width = 1.5f) { polyline(listOf(Offset(10f, 8f), Offset(14f, 12f), Offset(10f, 16f))) }
}

internal fun moreIcon() = icon("More") {
    fill {
        circle(Offset(6f, 12f), 1.5f)
        circle(Center, 1.5f)
        circle(Offset(18f, 12f), 1.5f)
    }
}

internal fun checkIcon() = icon("Check") {
    stroke(width = 1.5f) { polyline(listOf(Offset(6f, 12.5f), Offset(10f, 16.5f), Offset(18f, 8.5f))) }
}

internal fun doubleCheckIcon() = icon("DoubleCheck") {
    stroke(width = 1.5f) {
        polyline(listOf(Offset(2.5f, 12.5f), Offset(6f, 16f), Offset(13.5f, 8.5f)))
        polyline(listOf(Offset(10.5f, 14.5f), Offset(12f, 16f), Offset(19.5f, 8.5f)))
    }
}

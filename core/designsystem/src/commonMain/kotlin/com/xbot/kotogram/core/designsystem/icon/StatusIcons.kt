@file:Suppress("MagicNumber")

package com.xbot.kotogram.core.designsystem.icon

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import com.xbot.kotogram.core.designsystem.extensions.arc
import com.xbot.kotogram.core.designsystem.extensions.circle
import com.xbot.kotogram.core.designsystem.extensions.fill
import com.xbot.kotogram.core.designsystem.extensions.line
import com.xbot.kotogram.core.designsystem.extensions.polygon
import com.xbot.kotogram.core.designsystem.extensions.polyline
import com.xbot.kotogram.core.designsystem.extensions.roundRect
import com.xbot.kotogram.core.designsystem.extensions.stroke

internal fun bellIcon() = icon("Bell") { bell() }

internal fun bellOffIcon() = icon("BellOff") {
    val start = Offset(4.5f, 4.5f)
    val end = Offset(19.5f, 19.5f)
    val gap = 1.2f
    val belowSlash = halfPlane(start, end, gap, CANVAS)
    val aboveSlash = halfPlane(end, start, gap, CANVAS)
    group(clipPathData = clip { polygon(belowSlash); polygon(aboveSlash) }) {
        bell()
    }
    stroke(width = LINE) { line(start, end) }
}

internal fun clockIcon() = icon("Clock") {
    stroke(width = LINE) {
        circle(Center, 8.5f)
        polyline(listOf(Offset(12f, 7.5f), Offset(12f, 12f), Offset(15.5f, 14f)))
    }
}

internal fun lockIcon() = icon("Lock") {
    stroke(width = LINE) {
        roundRect(5.5f, 10.5f, 18.5f, 20f, 2.5f)
        moveTo(8.5f, 10.5f)
        lineTo(8.5f, 8f)
        arcTo(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15.5f, 8f)
        lineTo(15.5f, 10.5f)
    }
    fill { circle(Offset(12f, 15.25f), 1.25f) }
}

private fun ImageVector.Builder.bell() = stroke(width = LINE) {
    moveTo(6.5f, 16.5f)
    lineTo(6.5f, 11f)
    arcTo(5.5f, 5.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17.5f, 11f)
    lineTo(17.5f, 16.5f)
    line(Offset(4.5f, 16.5f), Offset(19.5f, 16.5f))
    line(Offset(12f, 3.8f), Offset(12f, 5.5f))
    arc(Offset(12f, 17.5f), 2f, 0f, 180f)
}

// A pencil pointing down to the left, centred on `center`.

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

internal fun speakerOffIcon() = icon("SpeakerOff") {
    stroke(1f) { line(Offset(8f, 7.5f), Offset(18f, 17.5f)) }
    fill {
        moveTo(8.428f, 9.695f)
        lineTo(15.333f, 16.601f)
        lineTo(15.333f, 17.021f)
        curveTo(15.333f, 17.165f, 15.291f, 17.306f, 15.214f, 17.427f)
        lineTo(15.149f, 17.513f)
        curveTo(14.902f, 17.797f, 14.488f, 17.85f, 14.18f, 17.653f)
        lineTo(14.091f, 17.587f)
        lineTo(11.016f, 14.912f)
        curveTo(10.864f, 14.78f, 10.677f, 14.698f, 10.479f, 14.674f)
        lineTo(10.359f, 14.667f)
        lineTo(8.667f, 14.667f)
        curveTo(8.154f, 14.667f, 7.731f, 14.281f, 7.673f, 13.783f)
        lineTo(7.667f, 13.667f)
        lineTo(7.667f, 10.667f)
        curveTo(7.667f, 10.197f, 7.991f, 9.802f, 8.428f, 9.695f)
        close()
        moveTo(14.476f, 6.57f)
        lineTo(14.583f, 6.563f)
        curveTo(14.963f, 6.563f, 15.277f, 6.845f, 15.326f, 7.211f)
        lineTo(15.333f, 7.313f)
        lineTo(15.333f, 13.065f)
        lineTo(11.376f, 9.108f)
        lineTo(14.091f, 6.747f)
        curveTo(14.2f, 6.652f, 14.334f, 6.591f, 14.476f, 6.57f)
        close()
    }
}

internal fun bellOffIcon() = icon("BellOff") {
    val start = Offset(4.5f, 4.5f)
    val end = Offset(19.5f, 19.5f)
    val gap = 1.2f
    val belowSlash = halfPlane(start, end, gap, CANVAS)
    val aboveSlash = halfPlane(end, start, gap, CANVAS)
    group(
        clipPathData = clip {
            polygon(belowSlash)
            polygon(aboveSlash)
        },
    ) {
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

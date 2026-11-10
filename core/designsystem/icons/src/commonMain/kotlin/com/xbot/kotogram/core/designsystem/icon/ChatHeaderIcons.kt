@file:Suppress("MagicNumber")

package com.xbot.kotogram.core.designsystem.icon

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.group
import com.xbot.kotogram.core.designsystem.extensions.circle
import com.xbot.kotogram.core.designsystem.extensions.fill
import com.xbot.kotogram.core.designsystem.extensions.line
import com.xbot.kotogram.core.designsystem.extensions.polygon
import com.xbot.kotogram.core.designsystem.extensions.roundRect
import com.xbot.kotogram.core.designsystem.extensions.stroke

internal fun callIcon() = icon("Call") {
    group(rotate = -45f, pivotX = 12f, pivotY = 12f) {
        stroke(width = LINE) {
            moveTo(9f, 3.5f)
            lineTo(13f, 3.5f)
            arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14.5f, 5f)
            lineTo(14.5f, 8.5f)
            arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13f, 10f)
            lineTo(11.5f, 10f)
            lineTo(11.5f, 14f)
            lineTo(13f, 14f)
            arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14.5f, 15.5f)
            lineTo(14.5f, 19f)
            arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13f, 20.5f)
            lineTo(9f, 20.5f)
            curveTo(5.5f, 17f, 5.5f, 7f, 9f, 3.5f)
            close()
        }
    }
}

internal fun videoIcon() = icon("Video") {
    stroke(width = LINE) {
        roundRect(3.5f, 7f, 15.5f, 17f, 2.5f)
        polygon(listOf(Offset(15.5f, 10.5f), Offset(20.5f, 7.5f), Offset(20.5f, 16.5f), Offset(15.5f, 13.5f)))
    }
}

internal fun infoIcon() = icon("Info") {
    stroke(width = LINE) {
        circle(Center, 8.5f)
        line(Offset(12f, 11f), Offset(12f, 16.5f))
    }
    fill { circle(Offset(12f, 8f), 0.9f) }
}

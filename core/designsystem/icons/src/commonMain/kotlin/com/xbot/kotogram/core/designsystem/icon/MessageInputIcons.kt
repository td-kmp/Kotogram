@file:Suppress("MagicNumber")

package com.xbot.kotogram.core.designsystem.icon

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.group
import com.xbot.kotogram.core.designsystem.extensions.arc
import com.xbot.kotogram.core.designsystem.extensions.circle
import com.xbot.kotogram.core.designsystem.extensions.fill
import com.xbot.kotogram.core.designsystem.extensions.line
import com.xbot.kotogram.core.designsystem.extensions.polygon
import com.xbot.kotogram.core.designsystem.extensions.polyline
import com.xbot.kotogram.core.designsystem.extensions.roundRect
import com.xbot.kotogram.core.designsystem.extensions.stroke

internal fun attachIcon() = icon("Attach") {
    group(rotate = 45f, pivotX = 12f, pivotY = 12f) {
        stroke(width = LINE) {
            moveTo(14.5f, 8f)
            lineTo(14.5f, 15.5f)
            arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.5f, 15.5f)
            lineTo(9.5f, 6.5f)
            arcTo(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16.5f, 6.5f)
            lineTo(16.5f, 16f)
            arcTo(4.5f, 4.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7.5f, 16f)
            lineTo(7.5f, 9.5f)
        }
    }
}

internal fun sendIcon() = icon("Send") {
    stroke(width = LINE) {
        polygon([Offset(4.5f, 4.5f), Offset(20f, 12f), Offset(4.5f, 19.5f), Offset(7.5f, 12f)])
        line(Offset(7.5f, 12f), Offset(13f, 12f))
    }
}

internal fun microphoneIcon() = icon("Microphone") {
    stroke(width = LINE) {
        roundRect(9f, 3.5f, 15f, 13.5f, 3f)
        arc(Offset(12f, 10.5f), 5.5f, 0f, 180f)
        line(Offset(12f, 16f), Offset(12f, 20f))
        line(Offset(9f, 20f), Offset(15f, 20f))
    }
}

internal fun smileIcon() = icon("Smile") {
    stroke(width = LINE) {
        circle(Center, 8.5f)
        arc(Offset(12f, 12.5f), 3.5f, 30f, 120f)
    }
    fill {
        circle(Offset(9f, 10f), 1f)
        circle(Offset(15f, 10f), 1f)
    }
}

internal fun cameraIcon() = icon("Camera") {
    stroke(width = LINE) {
        roundRect(3.5f, 7.5f, 20.5f, 19f, 2.5f)
        polyline([Offset(8.5f, 7.5f), Offset(9.8f, 5.2f), Offset(14.2f, 5.2f), Offset(15.5f, 7.5f)])
        circle(Offset(12f, 13f), 3.5f)
    }
}

@file:Suppress("MagicNumber")

package com.xbot.kotogram.core.designsystem.icon

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.group
import com.xbot.kotogram.core.designsystem.extensions.circle
import com.xbot.kotogram.core.designsystem.extensions.circleWithBite
import com.xbot.kotogram.core.designsystem.extensions.ellipse
import com.xbot.kotogram.core.designsystem.extensions.fill
import com.xbot.kotogram.core.designsystem.extensions.line
import com.xbot.kotogram.core.designsystem.extensions.polygon
import com.xbot.kotogram.core.designsystem.extensions.roundRect
import com.xbot.kotogram.core.designsystem.extensions.stroke

internal fun eyeIcon() = icon("Eye") {
    stroke(width = LINE) { ellipse(Center, EYE_RADIUS_X, EYE_RADIUS_Y) }
    fill { circleWithBite(Center, PUPIL_RADIUS, Offset(13f, 11f), 1f) }
}

internal fun eyeOffIcon() = icon("EyeOff") {
    val start = Offset(7.55f, 6.1f)
    val end = Offset(18.4f, 16.95f)
    val gap = 1.45f
    val belowSlash = halfPlane(start, end, gap, CANVAS)
    val aboveSlash = halfPlane(end, start, gap, CANVAS)
    group(clipPathData = clip { polygon(belowSlash); polygon(aboveSlash) }) {
        stroke(width = LINE) { ellipse(Center, EYE_RADIUS_X, EYE_RADIUS_Y) }
    }
    group(clipPathData = clip { polygon(belowSlash) }) {
        fill { circle(Center, PUPIL_RADIUS) }
    }
    stroke(width = 1.45f) { line(start, end) }
}

internal fun qrCodeIcon() = icon("QrCode") {
    stroke(width = LINE) {
        roundRect(4f, 4f, 10f, 10f, 1.5f)
        roundRect(14f, 4f, 20f, 10f, 1.5f)
        roundRect(4f, 14f, 10f, 20f, 1.5f)
    }
    fill {
        roundRect(6f, 6f, 8f, 8f, 0.5f)
        roundRect(16f, 6f, 18f, 8f, 0.5f)
        roundRect(6f, 16f, 8f, 18f, 0.5f)
        roundRect(14f, 14f, 16f, 16f, 0.5f)
        roundRect(18f, 14f, 20f, 16f, 0.5f)
        roundRect(16f, 16f, 18f, 18f, 0.5f)
        roundRect(14f, 18f, 16f, 20f, 0.5f)
        roundRect(18f, 18f, 20f, 20f, 0.5f)
    }
}

internal fun smartphoneIcon() = icon("Smartphone") {
    stroke(width = LINE) {
        roundRect(7f, 3.5f, 17f, 20.5f, 2.5f)
        line(Offset(10.5f, 17.5f), Offset(13.5f, 17.5f))
    }
}

private const val EYE_RADIUS_X = 5.8f
private const val EYE_RADIUS_Y = 3.95f
private const val PUPIL_RADIUS = 2f

@file:Suppress("MagicNumber")

package com.xbot.kotogram.core.designsystem.icon

import androidx.compose.ui.geometry.Offset
import com.xbot.kotogram.core.designsystem.extensions.arc
import com.xbot.kotogram.core.designsystem.extensions.circle
import com.xbot.kotogram.core.designsystem.extensions.ellipse
import com.xbot.kotogram.core.designsystem.extensions.fill
import com.xbot.kotogram.core.designsystem.extensions.gear
import com.xbot.kotogram.core.designsystem.extensions.line
import com.xbot.kotogram.core.designsystem.extensions.polyline
import com.xbot.kotogram.core.designsystem.extensions.roundRect
import com.xbot.kotogram.core.designsystem.extensions.stroke

internal fun settingsIcon() = icon("Settings") {
    stroke(width = LINE) {
        gear(Center, innerRadius = 6.6f, outerRadius = 8.6f, teeth = 8, toothDegrees = 16f)
        circle(Center, 3f)
    }
}

internal fun globeIcon() = icon("Globe") {
    stroke(width = LINE) {
        circle(Center, 8.5f)
        ellipse(Center, 3.5f, 8.5f)
        line(Offset(3.5f, 12f), Offset(20.5f, 12f))
    }
}

internal fun appearanceIcon() = icon("Appearance") {
    stroke(width = LINE) { circle(Center, 8.5f) }
    fill {
        moveTo(12f, 3.5f)
        arcTo(8.5f, 8.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 20.5f)
        close()
    }
}

internal fun storageIcon() = icon("Storage") {
    stroke(width = LINE) {
        ellipse(Offset(12f, 6.5f), 7f, 2.5f)
        line(Offset(5f, 6.5f), Offset(5f, 17.5f))
        line(Offset(19f, 6.5f), Offset(19f, 17.5f))
        moveTo(19f, 12f)
        arcTo(7f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5f, 12f)
        moveTo(19f, 17.5f)
        arcTo(7f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5f, 17.5f)
    }
}

internal fun devicesIcon() = icon("Devices") {
    stroke(width = LINE) {
        roundRect(5f, 5f, 19f, 15.5f, 1.5f)
        line(Offset(2.5f, 18.5f), Offset(21.5f, 18.5f))
    }
}

internal fun helpIcon() = icon("Help") {
    stroke(width = LINE) {
        circle(Center, 8.5f)
        arc(Offset(12f, 10f), 2.5f, 180f, 200f)
        lineTo(12f, 13f)
        lineTo(12f, 13.8f)
    }
    fill { circle(Offset(12f, 16.75f), 0.9f) }
}

internal fun logoutIcon() = icon("Logout") {
    stroke(width = LINE) {
        moveTo(12.5f, 4.5f)
        lineTo(7f, 4.5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5f, 6.5f)
        lineTo(5f, 17.5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7f, 19.5f)
        lineTo(12.5f, 19.5f)
        line(Offset(10f, 12f), Offset(20f, 12f))
        polyline(listOf(Offset(16.5f, 8.5f), Offset(20f, 12f), Offset(16.5f, 15.5f)))
    }
}

internal fun shieldIcon() = icon("Shield") {
    stroke(width = LINE) {
        moveTo(12f, 3.5f)
        lineTo(19f, 6f)
        lineTo(19f, 11.5f)
        curveTo(19f, 16f, 15.5f, 19.5f, 12f, 20.5f)
        curveTo(8.5f, 19.5f, 5f, 16f, 5f, 11.5f)
        lineTo(5f, 6f)
        close()
    }
}

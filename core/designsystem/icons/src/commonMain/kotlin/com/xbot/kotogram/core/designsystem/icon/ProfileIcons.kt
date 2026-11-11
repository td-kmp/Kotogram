@file:Suppress("MagicNumber")

package com.xbot.kotogram.core.designsystem.icon

import androidx.compose.ui.geometry.Offset
import com.xbot.kotogram.core.designsystem.extensions.arc
import com.xbot.kotogram.core.designsystem.extensions.circle
import com.xbot.kotogram.core.designsystem.extensions.line
import com.xbot.kotogram.core.designsystem.extensions.stroke

internal fun personIcon() = icon("Person") {
    stroke(width = LINE) {
        circle(Offset(12f, 8f), 3.5f)
        arc(Offset(12f, 20f), 7f, 180f, 180f)
    }
}

internal fun personAddIcon() = icon("PersonAdd") {
    stroke(width = LINE) {
        circle(Offset(10f, 8f), 3.5f)
        arc(Offset(10f, 20f), 6.5f, 180f, 180f)
        line(Offset(18.5f, 7.5f), Offset(18.5f, 13.5f))
        line(Offset(15.5f, 10.5f), Offset(21.5f, 10.5f))
    }
}

internal fun atSignIcon() = icon("AtSign") {
    stroke(width = LINE) {
        circle(Center, 3.5f)
        moveTo(15.5f, 8.5f)
        lineTo(15.5f, 13.25f)
        arcTo(2.25f, 2.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 20f, 13.25f)
        lineTo(20f, 12f)
        arc(Center, 8f, 0f, -310f)
    }
}

internal fun shareIcon() = icon("Share") {
    stroke(width = LINE) {
        circle(Offset(17.5f, 6f), 2.25f)
        circle(Offset(6.5f, 12f), 2.25f)
        circle(Offset(17.5f, 18f), 2.25f)
        line(Offset(8.5f, 10.9f), Offset(15.5f, 7.1f))
        line(Offset(8.5f, 13.1f), Offset(15.5f, 16.9f))
    }
}

internal fun blockIcon() = icon("Block") {
    stroke(width = LINE) {
        circle(Center, 8.5f)
        line(Offset(6f, 6f), Offset(18f, 18f))
    }
}

internal fun plusIcon() = icon("Plus") {
    stroke(width = LINE) {
        line(Offset(12f, 5f), Offset(12f, 19f))
        line(Offset(5f, 12f), Offset(19f, 12f))
    }
}

internal fun messageIcon() = icon("Message") {
    stroke(width = LINE) {
        moveTo(7f, 5f)
        lineTo(17f, 5f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20f, 8f)
        lineTo(20f, 13.5f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17f, 16.5f)
        lineTo(10.5f, 16.5f)
        lineTo(6.5f, 19.5f)
        lineTo(7f, 16.5f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 13.5f)
        lineTo(4f, 8f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7f, 5f)
        close()
    }
}

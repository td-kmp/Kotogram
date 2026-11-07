@file:Suppress("MagicNumber")

package com.xbot.kotogram.core.designsystem.icon

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import com.xbot.kotogram.core.designsystem.extensions.line
import com.xbot.kotogram.core.designsystem.extensions.polyline
import com.xbot.kotogram.core.designsystem.extensions.roundRect
import com.xbot.kotogram.core.designsystem.extensions.stroke

internal fun composeIcon() = icon("Compose") {
    stroke(width = LINE) {
        moveTo(12f, 5f)
        lineTo(7.5f, 5f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.5f, 8f)
        lineTo(4.5f, 16.5f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.5f, 19.5f)
        lineTo(16.5f, 19.5f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19.5f, 16.5f)
        lineTo(19.5f, 12f)
    }
    pencil(Offset(15.5f, 8.5f), length = 11f, width = 2.6f)
}

internal fun editIcon() = icon("Edit") {
    pencil(Center, length = 15f, width = 3f)
}

internal fun pinIcon() = icon("Pin") {
    group(rotate = 45f, pivotX = 12f, pivotY = 12f) {
        stroke(width = LINE) {
            roundRect(9f, 3.5f, 15f, 6f, 1f)
            polyline(
                listOf(
                    Offset(10f, 6f),
                    Offset(10f, 10.5f),
                    Offset(7.5f, 13.5f),
                    Offset(16.5f, 13.5f),
                    Offset(14f, 10.5f),
                    Offset(14f, 6f)
                )
            )
            line(Offset(12f, 13.5f), Offset(12f, 20.5f))
        }
    }
}

internal fun archiveIcon() = icon("Archive") {
    stroke(width = LINE) {
        roundRect(4f, 4.5f, 20f, 8.5f, 1.5f)
        moveTo(5.5f, 8.5f)
        lineTo(5.5f, 17.5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.5f, 19.5f)
        lineTo(16.5f, 19.5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18.5f, 17.5f)
        lineTo(18.5f, 8.5f)
        line(Offset(10f, 12f), Offset(14f, 12f))
    }
}

internal fun folderIcon() = icon("Folder") {
    stroke(width = LINE) {
        moveTo(3.5f, 8f)
        lineTo(3.5f, 17f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.5f, 19f)
        lineTo(18.5f, 19f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 20.5f, 17f)
        lineTo(20.5f, 10f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18.5f, 8f)
        lineTo(11.5f, 8f)
        lineTo(9.5f, 6f)
        lineTo(5.5f, 6f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.5f, 8f)
        close()
    }
}

internal fun bookmarkIcon() = icon("Bookmark") {
    stroke(width = LINE) {
        moveTo(6.5f, 19.5f)
        lineTo(6.5f, 6.5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8.5f, 4.5f)
        lineTo(15.5f, 4.5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17.5f, 6.5f)
        lineTo(17.5f, 19.5f)
        lineTo(12f, 15.5f)
        close()
    }
}

private fun ImageVector.Builder.pencil(center: Offset, length: Float, width: Float) {
    group(rotate = 45f, pivotX = center.x, pivotY = center.y) {
        val left = center.x - width / 2
        val right = center.x + width / 2
        val top = center.y - length / 2
        val tip = center.y + length / 2
        val bodyEnd = tip - width * 1.2f
        stroke(width = LINE) {
            roundRect(left, top, right, bodyEnd, width / 4)
            polyline(listOf(Offset(left, bodyEnd), Offset(center.x, tip), Offset(right, bodyEnd)))
            line(Offset(left, top + width), Offset(right, top + width))
        }
    }
}

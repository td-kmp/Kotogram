package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path

internal fun ImageVector.Builder.stroke(width: Float, block: PathBuilder.() -> Unit) = path(
    fill = null,
    stroke = SolidColor(Color.Black),
    strokeLineWidth = width,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
    pathBuilder = block
)

internal fun ImageVector.Builder.fill(
    fillType: PathFillType = PathFillType.NonZero,
    block: PathBuilder.() -> Unit
) = path(fill = SolidColor(Color.Black), pathFillType = fillType, pathBuilder = block)

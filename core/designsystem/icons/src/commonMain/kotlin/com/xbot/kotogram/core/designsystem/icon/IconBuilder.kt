package com.xbot.kotogram.core.designsystem.icon

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.unit.dp

internal const val CANVAS = 24f
internal const val LINE = 1.33f
internal val Center = Offset(12f, 12f)

internal fun icon(
    name: String,
    canvas: Float = CANVAS,
    width: Float = canvas,
    height: Float = canvas,
    block: ImageVector.Builder.() -> Unit,
): ImageVector = ImageVector.Builder(
    name = "KotogramIcons.$name",
    defaultWidth = width.dp,
    defaultHeight = height.dp,
    viewportWidth = width,
    viewportHeight = height,
).apply(block).build()

internal fun clip(block: PathBuilder.() -> Unit) = PathBuilder().apply(block).nodes

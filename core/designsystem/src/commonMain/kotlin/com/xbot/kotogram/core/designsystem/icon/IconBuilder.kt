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
    block: ImageVector.Builder.() -> Unit,
): ImageVector = ImageVector.Builder(
    name = "KotogramIcons.$name",
    defaultWidth = CANVAS.dp,
    defaultHeight = CANVAS.dp,
    viewportWidth = CANVAS,
    viewportHeight = CANVAS,
).apply(block).build()

internal fun clip(block: PathBuilder.() -> Unit) = PathBuilder().apply(block).nodes

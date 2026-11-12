package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import kotlin.math.abs

internal fun Path.moveTo(point: Offset) = moveTo(point.x, point.y)

internal fun Path.lineTo(point: Offset) = lineTo(point.x, point.y)

internal fun Path.addRectStack(
    rects: List<Rect>,
    radius: Float,
    smoothing: Float = 0f,
    snap: Float = 0f,
) {
    val rows = rects.filter { it.width > 0f && it.height > 0f }
    if (rows.isEmpty()) return
    val lefts = FloatArray(rows.size) { rows[it].left }
    val rights = FloatArray(rows.size) { rows[it].right }
    repeat(rows.size) {
        for (index in 1 until rows.size) {
            if (abs(lefts[index] - lefts[index - 1]) < snap) {
                lefts[index] = minOf(lefts[index], lefts[index - 1])
                lefts[index - 1] = lefts[index]
            }
            if (abs(rights[index] - rights[index - 1]) < snap) {
                rights[index] = maxOf(rights[index], rights[index - 1])
                rights[index - 1] = rights[index]
            }
        }
    }
    val joints = FloatArray(rows.size - 1) { (rows[it].bottom + rows[it + 1].top) / 2 }
    val corner = CornerRounding(radius, smoothing)
    val vertices = ArrayList<Float>()
    val rounding = ArrayList<CornerRounding>()
    val last = rows.lastIndex
    vertices.addVertex(rounding, lefts[0], rows[0].top, corner)
    vertices.addVertex(rounding, rights[0], rows[0].top, corner)
    for (index in 0 until last) {
        val step = stepRounding(rights[index], rights[index + 1], radius, smoothing) ?: continue
        vertices.addVertex(rounding, rights[index], joints[index], step)
        vertices.addVertex(rounding, rights[index + 1], joints[index], step)
    }
    vertices.addVertex(rounding, rights[last], rows[last].bottom, corner)
    vertices.addVertex(rounding, lefts[last], rows[last].bottom, corner)
    for (index in last downTo 1) {
        val step = stepRounding(lefts[index], lefts[index - 1], radius, smoothing) ?: continue
        vertices.addVertex(rounding, lefts[index], joints[index - 1], step)
        vertices.addVertex(rounding, lefts[index - 1], joints[index - 1], step)
    }
    addPolygon(RoundedPolygon(vertices.toFloatArray(), perVertexRounding = rounding))
}

internal fun Path.addPolygon(polygon: RoundedPolygon) {
    val cubics = polygon.cubics
    if (cubics.isEmpty()) return
    moveTo(cubics[0].anchor0X, cubics[0].anchor0Y)
    for (cubic in cubics) {
        cubicTo(cubic.control0X, cubic.control0Y, cubic.control1X, cubic.control1Y, cubic.anchor1X, cubic.anchor1Y)
    }
    close()
}

private fun MutableList<Float>.addVertex(
    rounding: MutableList<CornerRounding>,
    x: Float,
    y: Float,
    corner: CornerRounding,
) {
    add(x)
    add(y)
    rounding += corner
}

private fun stepRounding(
    from: Float,
    to: Float,
    radius: Float,
    smoothing: Float,
): CornerRounding? {
    val step = abs(from - to)
    return if (step == 0f) null else CornerRounding(minOf(radius, step / 2), smoothing)
}

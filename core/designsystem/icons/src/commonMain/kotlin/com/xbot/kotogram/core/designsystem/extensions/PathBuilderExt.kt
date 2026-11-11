package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.PathBuilder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal fun PathBuilder.circle(
    center: Offset,
    radius: Float,
) = ellipse(center, radius, radius)

internal fun PathBuilder.ellipse(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
) {
    moveTo(center.x + radiusX, center.y)
    arcTo(radiusX, radiusY, 0f, isMoreThanHalf = false, isPositiveArc = true, center.x - radiusX, center.y)
    arcTo(radiusX, radiusY, 0f, isMoreThanHalf = false, isPositiveArc = true, center.x + radiusX, center.y)
    close()
}

internal fun PathBuilder.polygon(points: List<Offset>) {
    polyline(points)
    close()
}

internal fun PathBuilder.polyline(points: List<Offset>) {
    points.forEachIndexed { index, point -> if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y) }
}

internal fun PathBuilder.line(
    from: Offset,
    to: Offset,
) {
    moveTo(from.x, from.y)
    lineTo(to.x, to.y)
}

internal fun PathBuilder.roundRect(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radius: Float,
) {
    moveTo(left + radius, top)
    lineTo(right - radius, top)
    arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, right, top + radius)
    lineTo(right, bottom - radius)
    arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, right - radius, bottom)
    lineTo(left + radius, bottom)
    arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, left, bottom - radius)
    lineTo(left, top + radius)
    arcTo(radius, radius, 0f, isMoreThanHalf = false, isPositiveArc = true, left + radius, top)
    close()
}

internal fun PathBuilder.arc(
    center: Offset,
    radius: Float,
    startDegrees: Float,
    sweepDegrees: Float,
) {
    val start = center + Offset.polar(radius, startDegrees)
    val end = center + Offset.polar(radius, startDegrees + sweepDegrees)
    moveTo(start.x, start.y)
    arcTo(
        radius,
        radius,
        0f,
        isMoreThanHalf = abs(sweepDegrees) > HALF_TURN,
        isPositiveArc = sweepDegrees > 0f,
        end.x,
        end.y,
    )
}

internal fun PathBuilder.gear(
    center: Offset,
    innerRadius: Float,
    outerRadius: Float,
    teeth: Int,
    toothDegrees: Float,
) {
    val step = 2 * HALF_TURN / teeth
    val flank = toothDegrees / 2
    val points = (0 until teeth).flatMap { tooth ->
        val angle = tooth * step - HALF_TURN / 2
        listOf(
            center + Offset.polar(innerRadius, angle - flank - FLANK_SLOPE),
            center + Offset.polar(outerRadius, angle - flank),
            center + Offset.polar(outerRadius, angle + flank),
            center + Offset.polar(innerRadius, angle + flank + FLANK_SLOPE),
        )
    }
    polygon(points)
}

internal fun PathBuilder.cross(
    center: Offset,
    halfExtent: Float,
    halfWidth: Float,
) {
    val reach = halfExtent * sqrt(2f)
    val corner = halfWidth * sqrt(2f)
    repeat(ARMS) { arm ->
        val angle = PI / 4 + arm * PI / 2
        val direction = Offset(cos(angle).toFloat(), sin(angle).toFloat())
        val across = Offset(-direction.y, direction.x)
        val end = center + direction * reach
        val from = end - across * halfWidth
        val to = end + across * halfWidth
        if (arm == 0) moveTo(from.x, from.y) else lineTo(from.x, from.y)
        arcTo(halfWidth, halfWidth, 0f, isMoreThanHalf = false, isPositiveArc = true, to.x, to.y)
        val cornerAngle = angle + PI / 4
        lineTo(center.x + corner * cos(cornerAngle).toFloat(), center.y + corner * sin(cornerAngle).toFloat())
    }
    close()
}

internal fun PathBuilder.circleWithBite(
    center: Offset,
    radius: Float,
    biteCenter: Offset,
    biteRadius: Float,
) {
    val distance = (biteCenter - center).getDistance()
    val along = (radius * radius - biteRadius * biteRadius + distance * distance) / (2 * distance)
    val half = sqrt(radius * radius - along * along)
    val unit = (biteCenter - center) / distance
    val base = center + unit * along
    val normal = Offset(-unit.y, unit.x)
    var start = base + normal * half
    var end = base - normal * half
    if (sweep(center, start, end) < PI) start = end.also { end = start }
    moveTo(start.x, start.y)
    arcTo(radius, radius, 0f, isMoreThanHalf = true, isPositiveArc = true, end.x, end.y)
    val inner = sweep(biteCenter, end, start)
    val inside = biteCenter - unit * biteRadius
    val throughInside = sweep(biteCenter, end, inside) < inner
    arcTo(
        biteRadius,
        biteRadius,
        0f,
        isMoreThanHalf = if (throughInside) inner > PI else inner < PI,
        isPositiveArc = throughInside,
        start.x,
        start.y,
    )
    close()
}

private fun sweep(
    center: Offset,
    from: Offset,
    to: Offset,
): Double {
    val start = atan2((from.y - center.y).toDouble(), (from.x - center.x).toDouble())
    val end = atan2((to.y - center.y).toDouble(), (to.x - center.x).toDouble())
    return ((end - start) % (2 * PI) + 2 * PI) % (2 * PI)
}

private const val ARMS = 4
private const val HALF_TURN = 180f
private const val FLANK_SLOPE = 6f

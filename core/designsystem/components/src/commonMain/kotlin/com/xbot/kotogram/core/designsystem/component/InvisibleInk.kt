package com.xbot.kotogram.core.designsystem.component

import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

internal fun Modifier.invisibleInk(
    color: Color,
    areas: () -> List<Rect>,
): Modifier = this then InvisibleInkElement(color, areas)

private class InvisibleInkElement(
    private val color: Color,
    private val areas: () -> List<Rect>,
) : ModifierNodeElement<InvisibleInkNode>() {
    override fun create() = InvisibleInkNode(color, areas)

    override fun update(node: InvisibleInkNode) {
        node.color = color
        node.areas = areas
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "invisibleInk"
        properties["color"] = color
    }

    override fun equals(other: Any?) = other is InvisibleInkElement && other.color == color && other.areas === areas

    override fun hashCode() = color.hashCode() * HASH_PRIME + areas.hashCode()
}

private class InvisibleInkNode(
    var color: Color,
    var areas: () -> List<Rect>,
) : Modifier.Node(),
    DrawModifierNode {
    private val paint = Paint().apply { strokeCap = StrokeCap.Round }
    private var dust: Dust? = null
    private var seconds = 0.0

    override fun onAttach() {
        coroutineScope.launch {
            val start = withFrameNanos { it }
            while (true) {
                withFrameNanos { seconds = (it - start) / NANOS_IN_SECOND }
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        val rects = areas()
        if (rects.isEmpty()) return
        val unit = 1.dp.toPx()
        val current = dust?.takeIf { it.areas === rects && it.unit == unit } ?: Dust(rects, unit).also { dust = it }
        current.advance(seconds)
        val bleed = BLEED * unit
        paint.strokeWidth = SPECKLE * unit
        clipRect(-bleed, -bleed, size.width + bleed, size.height + bleed) {
            drawIntoCanvas { canvas -> current.draw(canvas, paint, color) }
        }
    }
}

private class Dust(
    val areas: List<Rect>,
    val unit: Float,
) {
    private val shares = FloatArray(areas.size)
    private val count: Int
    private val opaque = SpeckleBatch()
    private val faint: FloatArray
    private var faintSize = 0

    init {
        var total = 0f
        areas.forEachIndexed { index, area ->
            total += area.width * area.height
            shares[index] = total
        }
        for (index in shares.indices) shares[index] /= total.coerceAtLeast(1f)
        count = (total / (unit * unit) * DENSITY).roundToInt().coerceAtMost(MAX_SPECKLES)
        faint = FloatArray(count * FAINT_STRIDE)
    }

    fun advance(seconds: Double) {
        opaque.clear()
        faintSize = 0
        val lives = seconds / LIFETIME
        for (index in 0 until count) {
            val life = lives + unitHash(index, PHASE)
            val generation = floor(life)
            val age = (life - generation).toFloat()
            val seed = hash(index, generation.toInt())
            val alpha = speckleAlpha(seed, age)
            if (alpha < MIN_VISIBLE) continue
            val area = areas[areaIndex(unitHash(seed, AREA))]
            val angle = unitHash(seed, ANGLE) * FULL_TURN
            val travel = speckleSpeed(seed) * unit * age * LIFETIME
            val x = area.left + unitHash(seed, X) * area.width + cos(angle) * travel
            val y = area.top + unitHash(seed, Y) * area.height + sin(angle) * travel
            if (alpha >= 1f) {
                opaque.add(x, y)
            } else {
                faint[faintSize] = x
                faint[faintSize + 1] = y
                faint[faintSize + 2] = alpha
                faintSize += FAINT_STRIDE
            }
        }
    }

    fun draw(
        canvas: Canvas,
        paint: Paint,
        color: Color,
    ) {
        paint.color = color
        opaque.draw(canvas, paint)
        val radius = paint.strokeWidth / 2
        for (at in 0 until faintSize step FAINT_STRIDE) {
            paint.color = color.copy(alpha = color.alpha * faint[at + 2])
            canvas.drawCircle(Offset(faint[at], faint[at + 1]), radius, paint)
        }
    }

    private fun areaIndex(share: Float): Int {
        for (index in shares.indices) if (share < shares[index]) return index
        return shares.lastIndex
    }
}

private class SpeckleBatch {
    private val chunks = ArrayList<FloatArray>()
    private var size = 0

    fun clear() {
        size = 0
    }

    fun add(
        x: Float,
        y: Float,
    ) {
        val chunk = size / CHUNK
        if (chunk == chunks.size) chunks += FloatArray(CHUNK)
        val at = size % CHUNK
        chunks[chunk][at] = x
        chunks[chunk][at + 1] = y
        size += 2
    }

    fun draw(
        canvas: Canvas,
        paint: Paint,
    ) {
        if (size == 0) return
        val full = size / CHUNK
        val rest = size % CHUNK
        // Hack: Canvas draws a whole array, so the unused end of the last chunk goes off screen.
        if (rest > 0) chunks[full].fill(OFF_SCREEN, rest, CHUNK)
        for (index in 0 until if (rest > 0) full + 1 else full) {
            canvas.drawRawPoints(PointMode.Points, chunks[index], paint)
        }
    }
}

internal fun speckleAlpha(
    seed: Int,
    age: Float,
): Float = (MIN_ALPHA + unitHash(seed, ALPHA) + alphaOverLife(age)).coerceIn(0f, 1f)

internal fun speckleSpeed(seed: Int): Float {
    val tilt = unitHash(seed, TILT) * 2f - 1f
    return unitHash(seed, SPEED) * MAX_SPEED * sqrt(1f - tilt * tilt)
}

private fun alphaOverLife(age: Float): Float = when {
    age < FADE_IN_START -> 0f
    age < PEAK -> (age - FADE_IN_START) / (PEAK - FADE_IN_START)
    age < FADE_OUT_START -> 1f - (age - PEAK) / (FADE_OUT_START - PEAK)
    else -> -(age - FADE_OUT_START) / (1f - FADE_OUT_START)
}

private fun unitHash(
    seed: Int,
    salt: Int,
): Float = (hash(seed, salt) ushr HASH_DROP) * HASH_UNIT

@Suppress("MagicNumber")
private fun hash(
    seed: Int,
    salt: Int,
): Int {
    var value = seed * -0x61c88647 + salt * -0x7a143595
    value = (value xor (value ushr 16)) * -0x7a143595
    value = (value xor (value ushr 13)) * -0x3d4d51cb
    return value xor (value ushr 16)
}

private const val DENSITY = 0.35f
private const val MAX_SPECKLES = 2000
private const val LIFETIME = 1f
private const val MAX_SPEED = 10f
private const val SPECKLE = 1.1f
private const val BLEED = 3f
private const val MIN_ALPHA = 0.5f
private const val MIN_VISIBLE = 0.02f
private const val FAINT_STRIDE = 3
private const val FADE_IN_START = 0.25f
private const val PEAK = 0.5f
private const val FADE_OUT_START = 0.75f
private const val CHUNK = 128
private const val OFF_SCREEN = -1e6f
private const val FULL_TURN = (2 * PI).toFloat()
private const val NANOS_IN_SECOND = 1e9
private const val HASH_PRIME = 31
private const val HASH_DROP = 8
private const val HASH_UNIT = 1f / (1 shl 24)
private const val PHASE = 1
private const val ALPHA = 2
private const val AREA = 3
private const val ANGLE = 4
private const val SPEED = 5
private const val X = 6
private const val Y = 7
private const val TILT = 8

package com.xbot.kotogram.core.designsystem.component

import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntRect
import com.xbot.kotogram.core.designsystem.component.platform.FrameLayer
import com.xbot.kotogram.core.designsystem.component.platform.frameLayer
import com.xbot.kotogram.core.designsystem.extensions.drawSprites
import com.xbot.kotogram.core.designsystem.extensions.toSpritePaint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

internal fun Modifier.invisibleInk(
    color: Color,
    areas: () -> List<Rect>,
): Modifier = graphicsLayer() then InvisibleInkElement(color, areas) then Modifier.graphicsLayer()

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
    DrawModifierNode,
    CompositionLocalConsumerModifierNode {
    private val picture: (Canvas) -> Unit = { canvas -> dust?.draw(canvas, DustTime.seconds, color) }
    private var layer: FrameLayer? = null
    private var dust: Dust? = null
    private var frames: Job? = null

    override fun onAttach() {
        layer = frameLayer()
    }

    override fun onDetach() {
        frames = null
        dust = null
        layer?.release()
        layer = null
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        val layer = layer
        if (layer != null && hasDust(density)) {
            val bleed = ceil(BLEED * density).toInt()
            val bounds = IntRect(-bleed, -bleed, ceil(size.width).toInt() + bleed, ceil(size.height).toInt() + bleed)
            with(layer) { draw(bounds, picture) }
            if (frames == null) frames = coroutineScope.launch { animate(layer) }
        } else {
            frames?.cancel()
            frames = null
        }
    }

    private suspend fun animate(layer: FrameLayer) {
        while (true) {
            withFrameNanos { frameTimeNanos ->
                DustTime.advance(frameTimeNanos)
                val shown = isAttached && hasDust(requireDensity().density) && layer.update(picture)
                if (!shown && isAttached) invalidateDraw()
            }
        }
    }

    private fun hasDust(unit: Float): Boolean {
        val rects = areas()
        if (rects.isEmpty()) return false
        if (dust?.fits(rects, unit) != true) dust = Dust(rects.toList(), unit)
        return true
    }
}

private object DustTime {
    private var origin = NOT_STARTED

    var seconds = 0.0
        private set

    fun advance(frameTimeNanos: Long) {
        if (origin == NOT_STARTED) origin = frameTimeNanos
        seconds = (frameTimeNanos - origin) / NANOS_IN_SECOND
    }
}

private class Dust(
    private val areas: List<Rect>,
    private val unit: Float,
) {
    private val shares = FloatArray(areas.size)
    private val count: Int
    private val phases: FloatArray
    private val generations: IntArray
    private val startX: FloatArray
    private val startY: FloatArray
    private val driftX: FloatArray
    private val driftY: FloatArray
    private val alphas: FloatArray
    private val speckles: Speckles

    init {
        var total = 0f
        areas.forEachIndexed { index, area ->
            total += area.width * area.height
            shares[index] = total
        }
        for (index in shares.indices) shares[index] /= total.coerceAtLeast(1f)
        count = (total / (unit * unit) * DENSITY).roundToInt().coerceAtMost(MAX_SPECKLES)
        phases = FloatArray(count) { unitHash(it, PHASE) }
        generations = IntArray(count) { Int.MIN_VALUE }
        startX = FloatArray(count)
        startY = FloatArray(count)
        driftX = FloatArray(count)
        driftY = FloatArray(count)
        alphas = FloatArray(count)
        speckles = Speckles(count, SPECKLE * unit)
    }

    fun fits(
        areas: List<Rect>,
        unit: Float,
    ): Boolean = this.unit == unit && this.areas == areas

    fun draw(
        canvas: Canvas,
        seconds: Double,
        color: Color,
    ) {
        advance(seconds)
        speckles.draw(canvas, color)
    }

    private fun advance(seconds: Double) {
        speckles.clear()
        val lives = seconds / LIFETIME
        for (index in 0 until count) {
            val life = lives + phases[index]
            val generation = floor(life).toInt()
            if (generation != generations[index]) bear(index, generation)
            val age = (life - generation).toFloat()
            val alpha = (alphas[index] + alphaOverLife(age)).coerceIn(0f, 1f)
            if (alpha < MIN_VISIBLE) continue
            speckles.add(startX[index] + driftX[index] * age, startY[index] + driftY[index] * age, alpha)
        }
    }

    private fun bear(
        index: Int,
        generation: Int,
    ) {
        val seed = hash(index, generation)
        val area = areas[areaIndex(unitHash(seed, AREA))]
        val angle = unitHash(seed, ANGLE) * FULL_TURN
        val drift = speckleSpeed(seed) * unit * LIFETIME
        generations[index] = generation
        startX[index] = area.left + unitHash(seed, X) * area.width
        startY[index] = area.top + unitHash(seed, Y) * area.height
        driftX[index] = cos(angle) * drift
        driftY[index] = sin(angle) * drift
        alphas[index] = bornAlpha(seed)
    }

    private fun areaIndex(share: Float): Int {
        for (index in shares.indices) if (share < shares[index]) return index
        return shares.lastIndex
    }
}

private class Speckles(
    capacity: Int,
    diameter: Float,
) {
    private val sprite = SpeckleSprite.of(diameter)
    private val half = sprite.side / 2f
    private val positions = FloatArray(capacity * QUAD_FLOATS)
    private val colors = IntArray(capacity * QUAD_CORNERS)
    private val alphas = FloatArray(capacity)
    private val textureCoordinates = FloatArray(capacity * QUAD_FLOATS)
    private val indices = ShortArray(capacity * QUAD_INDICES)
    private var size = 0
    private var drawn = 0

    init {
        val side = sprite.side.toFloat()
        val corners: FloatArray = [0f, 0f, side, 0f, side, side, 0f, side]
        for (at in textureCoordinates.indices) textureCoordinates[at] = corners[at % QUAD_FLOATS]
        for (at in indices.indices) {
            val quad = at / QUAD_INDICES
            indices[at] = (quad * QUAD_CORNERS + QuadTriangles[at % QUAD_INDICES]).toShort()
        }
    }

    fun clear() {
        size = 0
    }

    fun add(
        x: Float,
        y: Float,
        alpha: Float,
    ) {
        val left = x - half
        val top = y - half
        val right = x + half
        val bottom = y + half
        var at = size * QUAD_FLOATS
        positions[at++] = left
        positions[at++] = top
        positions[at++] = right
        positions[at++] = top
        positions[at++] = right
        positions[at++] = bottom
        positions[at++] = left
        positions[at] = bottom
        alphas[size++] = alpha
    }

    fun draw(
        canvas: Canvas,
        color: Color,
    ) {
        if (positions.isEmpty()) return
        val argb = color.toArgb()
        val rgb = argb and RGB_MASK
        val opacity = (argb ushr ALPHA_SHIFT).toFloat()
        for (index in 0 until size) {
            val tinted = ((alphas[index] * opacity + ROUNDING).toInt() shl ALPHA_SHIFT) or rgb
            colors.fill(tinted, index * QUAD_CORNERS, (index + 1) * QUAD_CORNERS)
        }
        // Hack: the canvases take whole arrays, so the quads left from the last frame collapse into points.
        if (size < drawn) positions.fill(0f, size * QUAD_FLOATS, drawn * QUAD_FLOATS)
        drawn = size
        canvas.drawSprites(positions, textureCoordinates, colors, indices, sprite.paint)
    }
}

private class SpeckleSprite(
    val side: Int,
    val paint: Paint,
) {
    companion object {
        private val sprites = HashMap<Float, SpeckleSprite>()

        fun of(diameter: Float): SpeckleSprite = sprites.getOrPut(diameter) {
            // Hack: an odd side puts the dot's centre in the middle of a pixel, which keeps a still sprite sharp.
            val side = (ceil(diameter).toInt() + 2 * SPRITE_MARGIN) or 1
            val bitmap = ImageBitmap(side, side)
            val center = Offset(side / 2f, side / 2f)
            Canvas(bitmap).drawCircle(center, diameter / 2f, Paint().apply { color = Color.White })
            SpeckleSprite(side, bitmap.toSpritePaint())
        }
    }
}

internal fun speckleAlpha(
    seed: Int,
    age: Float,
): Float = (bornAlpha(seed) + alphaOverLife(age)).coerceIn(0f, 1f)

private fun bornAlpha(seed: Int): Float = MIN_ALPHA + unitHash(seed, ALPHA)

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

private fun hash(
    seed: Int,
    salt: Int,
): Int {
    var value = seed * HASH_A + salt * HASH_B
    value = (value xor (value ushr HASH_SHIFT)) * HASH_B
    value = (value xor (value ushr HASH_SHIFT_SHORT)) * HASH_C
    return value xor (value ushr HASH_SHIFT)
}

private const val NOT_STARTED = Long.MIN_VALUE
private const val DENSITY = 0.35f
private const val MAX_SPECKLES = 2000
private const val LIFETIME = 1f
private const val MAX_SPEED = 10f
private const val SPECKLE = 1.1f
private const val BLEED = 3f
private const val MIN_ALPHA = 0.5f
private const val MIN_VISIBLE = 0.02f
private const val FADE_IN_START = 0.25f
private const val PEAK = 0.5f
private const val FADE_OUT_START = 0.75f
private const val QUAD_CORNERS = 4
private const val QUAD_FLOATS = 8
private const val QUAD_INDICES = 6
private val QuadTriangles: IntArray = [0, 1, 2, 0, 2, 3]
private const val SPRITE_MARGIN = 1
private const val RGB_MASK = 0xFFFFFF
private const val ALPHA_SHIFT = 24
private const val ROUNDING = 0.5f
private const val FULL_TURN = (2 * PI).toFloat()
private const val NANOS_IN_SECOND = 1e9
private const val HASH_PRIME = 31
private const val HASH_A = -0x61c88647
private const val HASH_B = -0x7a143595
private const val HASH_C = -0x3d4d51cb
private const val HASH_SHIFT = 16
private const val HASH_SHIFT_SHORT = 13
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

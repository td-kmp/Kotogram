package com.xbot.kotogram.core.designsystem.component

import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.component.platform.NativeDust
import com.xbot.kotogram.core.designsystem.component.platform.nativeDust
import com.xbot.kotogram.core.designsystem.extensions.drawPointBatch
import kotlinx.coroutines.Job
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
    GlobalPositionAwareModifierNode,
    CompositionLocalConsumerModifierNode {
    private val paint = Paint().apply { strokeCap = StrokeCap.Round }
    private var dust: Dust? = null
    private var frames: Job? = null
    private var seconds = DustClock.latestSeconds()
    private var native: NativeDust? = null
    private var nativeChecked = false

    override fun onDetach() {
        frames = null
        native?.release()
        native = null
        nativeChecked = false
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (!nativeChecked) {
            nativeChecked = true
            native = nativeDust(::onNativeFrame)?.also { invalidateDraw() }
        }
        native?.place(coordinates, with(currentValueOf(LocalDensity)) { BLEED.dp.toPx() })
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        val rects = areas()
        val unit = 1.dp.toPx()
        val native = native
        native?.show(rects, color, unit)
        val drawn = rects.isNotEmpty() && native?.drawing != true
        if (!drawn || native != null) {
            frames?.cancel()
            frames = null
        } else if (frames == null) {
            frames = coroutineScope.launch { animate() }
        }
        if (drawn) drawDust(rects, unit)
    }

    private fun ContentDrawScope.drawDust(
        rects: List<Rect>,
        unit: Float,
    ) {
        val current = dust?.takeIf { it.areas === rects && it.unit == unit } ?: Dust(rects, unit).also { dust = it }
        current.advance(seconds)
        val bleed = BLEED * unit
        paint.strokeWidth = SPECKLE * unit
        clipRect(-bleed, -bleed, size.width + bleed, size.height + bleed) {
            drawIntoCanvas { canvas -> current.draw(canvas, paint, color) }
        }
    }

    private fun onNativeFrame(frameTimeNanos: Long) {
        seconds = DustClock.seconds(frameTimeNanos)
        invalidateDraw()
    }

    private suspend fun animate() {
        while (true) {
            withFrameNanos { now -> seconds = DustClock.seconds(now) }
            invalidateDraw()
        }
    }
}

internal object DustClock {
    var originNanos = NOT_STARTED
        private set
    private var latestNanos = NOT_STARTED

    fun seconds(frameTimeNanos: Long): Double {
        if (originNanos == NOT_STARTED) originNanos = frameTimeNanos
        latestNanos = frameTimeNanos
        return dustSeconds(frameTimeNanos, originNanos)
    }

    fun latestSeconds(): Double = if (latestNanos == NOT_STARTED) 0.0 else seconds(latestNanos)
}

internal fun dustSeconds(
    frameTimeNanos: Long,
    originNanos: Long,
): Double = ((frameTimeNanos - originNanos) % DUST_PERIOD_NANOS).coerceAtLeast(0L) / NANOS_IN_SECOND

private class Dust(
    val areas: List<Rect>,
    val unit: Float,
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
    private val levels = Array(ALPHA_LEVELS + 1) { SpeckleBatch() }

    init {
        var total = 0f
        areas.forEachIndexed { index, area ->
            total += area.width * area.height
            shares[index] = total
        }
        for (index in shares.indices) shares[index] /= total.coerceAtLeast(1f)
        count = dustSpeckleCount(total, unit)
        phases = FloatArray(count) { unitHash(it, PHASE) }
        generations = IntArray(count) { Int.MIN_VALUE }
        startX = FloatArray(count)
        startY = FloatArray(count)
        driftX = FloatArray(count)
        driftY = FloatArray(count)
        alphas = FloatArray(count)
    }

    fun advance(seconds: Double) {
        for (level in levels) level.clear()
        val lives = seconds / LIFETIME
        for (index in 0 until count) {
            val life = lives + phases[index]
            val generation = floor(life).toInt()
            if (generation != generations[index]) bear(index, generation)
            val age = (life - generation).toFloat()
            val alpha = (alphas[index] + alphaOverLife(age)).coerceIn(0f, 1f)
            if (alpha < MIN_VISIBLE) continue
            val level = if (alpha >= 1f) ALPHA_LEVELS else (alpha * ALPHA_LEVELS).toInt()
            levels[level].add(startX[index] + driftX[index] * age, startY[index] + driftY[index] * age)
        }
    }

    fun draw(
        canvas: Canvas,
        paint: Paint,
        color: Color,
    ) {
        levels.forEachIndexed { level, batch ->
            if (batch.isEmpty) return@forEachIndexed
            val alpha = if (level == ALPHA_LEVELS) 1f else (level + LEVEL_MIDDLE) / ALPHA_LEVELS
            paint.color = color.copy(alpha = color.alpha * alpha)
            batch.draw(canvas, paint)
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

private class SpeckleBatch {
    private val chunks = ArrayList<FloatArray>()
    private var size = 0

    val isEmpty: Boolean get() = size == 0

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
        var left = size
        for (chunk in chunks) {
            if (left <= 0) break
            canvas.drawPointBatch(chunk, minOf(left, CHUNK), paint)
            left -= CHUNK
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

internal fun dustSpeckleCount(
    area: Float,
    unit: Float,
): Int = (area / (unit * unit) * DENSITY).roundToInt().coerceAtMost(MAX_SPECKLES)

internal fun dustVertexShader(): String =
    """
    #version 300 es
    precision highp float;
    precision highp int;
    uniform float uTime;
    uniform float uUnit;
    uniform vec2 uSize;
    uniform int uAreaCount;
    uniform vec4 uAreas[$DUST_MAX_AREAS];
    uniform float uShares[$DUST_MAX_AREAS];
    out float vAlpha;
    flat out float vDiameter;

    uint hash(uint seed, uint salt) {
        uint value = seed * ${HASH_A.toUInt()}u + salt * ${HASH_B.toUInt()}u;
        value = (value ^ (value >> ${HASH_SHIFT}u)) * ${HASH_B.toUInt()}u;
        value = (value ^ (value >> ${HASH_SHIFT_SHORT}u)) * ${HASH_C.toUInt()}u;
        return value ^ (value >> ${HASH_SHIFT}u);
    }

    float unitHash(uint seed, uint salt) {
        return float(hash(seed, salt) >> ${HASH_DROP}u) * $HASH_UNIT;
    }

    float alphaOverLife(float age) {
        if (age < $FADE_IN_START) return 0.0;
        if (age < $PEAK) return (age - $FADE_IN_START) / ($PEAK - $FADE_IN_START);
        if (age < $FADE_OUT_START) return 1.0 - (age - $PEAK) / ($FADE_OUT_START - $PEAK);
        return -(age - $FADE_OUT_START) / (1.0 - $FADE_OUT_START);
    }

    void main() {
        uint index = uint(gl_VertexID);
        float life = uTime / $LIFETIME + unitHash(index, ${PHASE}u);
        float generation = floor(life);
        float age = life - generation;
        uint seed = hash(index, uint(generation));
        float alpha = clamp($MIN_ALPHA + unitHash(seed, ${ALPHA}u) + alphaOverLife(age), 0.0, 1.0);
        float share = unitHash(seed, ${AREA}u);
        int area = uAreaCount - 1;
        for (int i = 0; i < uAreaCount && i < $DUST_MAX_AREAS; i++) {
            if (share < uShares[i]) { area = i; break; }
        }
        vec4 box = uAreas[area];
        float angle = unitHash(seed, ${ANGLE}u) * $FULL_TURN;
        float tilt = unitHash(seed, ${TILT}u) * 2.0 - 1.0;
        float travel = unitHash(seed, ${SPEED}u) * $MAX_SPEED * sqrt(1.0 - tilt * tilt) * uUnit * age * $LIFETIME;
        vec2 start = box.xy + vec2(unitHash(seed, ${X}u), unitHash(seed, ${Y}u)) * (box.zw - box.xy);
        vec2 position = start + vec2(cos(angle), sin(angle)) * travel;
        vAlpha = alpha;
        vDiameter = $SPECKLE * uUnit;
        gl_PointSize = vDiameter + 2.0;
        gl_Position = alpha < $MIN_VISIBLE
            ? vec4(2.0, 2.0, 0.0, 1.0)
            : vec4(position.x / uSize.x * 2.0 - 1.0, 1.0 - position.y / uSize.y * 2.0, 0.0, 1.0);
    }
    """.trimIndent()

internal fun dustFragmentShader(): String =
    """
    #version 300 es
    precision mediump float;
    uniform vec4 uColor;
    in float vAlpha;
    flat in float vDiameter;
    out vec4 fragColor;

    void main() {
        float distance = length(gl_PointCoord - vec2(0.5)) * (vDiameter + 2.0);
        float coverage = clamp(vDiameter * 0.5 + 0.5 - distance, 0.0, 1.0);
        float alpha = uColor.a * vAlpha * coverage;
        if (alpha <= 0.0) discard;
        fragColor = vec4(uColor.rgb * alpha, alpha);
    }
    """.trimIndent()

internal const val DUST_MAX_AREAS = 64

private const val NOT_STARTED = Long.MIN_VALUE
private const val DUST_PERIOD_NANOS = 3_600_000_000_000L

private const val DENSITY = 0.35f
private const val MAX_SPECKLES = 2000
private const val LIFETIME = 1f
private const val MAX_SPEED = 10f
private const val SPECKLE = 1.1f
private const val BLEED = 3f
private const val MIN_ALPHA = 0.5f
private const val MIN_VISIBLE = 0.02f
private const val ALPHA_LEVELS = 16
private const val LEVEL_MIDDLE = 0.5f
private const val FADE_IN_START = 0.25f
private const val PEAK = 0.5f
private const val FADE_OUT_START = 0.75f
private const val CHUNK = 128
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

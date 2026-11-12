package com.xbot.kotogram.core.designsystem.component.platform

import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.view.AttachedSurfaceControl
import android.view.Surface
import android.view.SurfaceControl
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import com.xbot.kotogram.core.designsystem.component.DUST_MAX_AREAS
import com.xbot.kotogram.core.designsystem.component.DustClock
import com.xbot.kotogram.core.designsystem.component.dustSpeckleCount
import kotlin.math.min
import androidx.compose.ui.geometry.Rect as ComposeRect

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class DustLayer(
    private val root: AttachedSurfaceControl,
    val width: Int,
    val height: Int,
) {
    private val control = SurfaceControl.Builder()
        .setName(LAYER_NAME)
        .setBufferSize(width, height)
        .setFormat(PixelFormat.TRANSLUCENT)
        .setOpaque(false)
        .setHidden(true)
        .build()
    private val target = DustTarget(Surface(control), onReleased = control::release)
    private var armedSwaps = 0

    val ready: Boolean get() = target.swaps > armedSwaps

    init {
        root.buildReparentTransaction(control)?.setLayer(control, ABOVE_CONTENT)?.apply()
        DustRenderer.add(target)
    }

    fun publish(
        areas: List<ComposeRect>,
        bleed: Float,
        color: Color,
        unit: Float,
    ) {
        target.frame = frame(areas, bleed, color, unit)
        DustRenderer.wake()
    }

    fun arm() {
        armedSwaps = target.swaps
        target.rendering = true
        DustRenderer.wake()
    }

    fun reveal(
        transaction: SurfaceControl.Transaction,
        position: Offset,
        crop: Rect,
    ) {
        root.buildReparentTransaction(control)?.let(transaction::merge)
        transaction.setLayer(control, ABOVE_CONTENT)
            .setPosition(control, position.x, position.y)
            .setCrop(control, crop)
            .setVisibility(control, true)
        target.rendering = true
        DustRenderer.wake()
    }

    fun conceal(transaction: SurfaceControl.Transaction) {
        transaction.setVisibility(control, false)
        target.rendering = false
    }

    fun release() {
        target.rendering = false
        SurfaceControl.Transaction().reparent(control, null).apply()
        DustRenderer.remove(target)
    }

    private fun frame(
        areas: List<ComposeRect>,
        bleed: Float,
        color: Color,
        unit: Float,
    ): DustFrame? {
        val count = min(areas.size, DUST_MAX_AREAS)
        if (count == 0) return null
        val boxes = FloatArray(count * BOX)
        val shares = FloatArray(count)
        var total = 0f
        for (index in 0 until count) {
            val area = areas[index]
            boxes[index * BOX + LEFT] = area.left + bleed
            boxes[index * BOX + TOP] = area.top + bleed
            boxes[index * BOX + RIGHT] = area.right + bleed
            boxes[index * BOX + BOTTOM] = area.bottom + bleed
            total += area.width * area.height
            shares[index] = total
        }
        for (index in 0 until count) shares[index] /= total.coerceAtLeast(1f)
        val srgb = color.convert(ColorSpaces.Srgb)
        return DustFrame(
            areas = boxes,
            shares = shares,
            areaCount = count,
            speckles = dustSpeckleCount(total, unit),
            color = floatArrayOf(srgb.red, srgb.green, srgb.blue, srgb.alpha),
            unit = unit,
            width = width,
            height = height,
            originNanos = DustClock.originNanos,
        )
    }
}

private const val LAYER_NAME = "Kotogram dust"
private const val ABOVE_CONTENT = 1
private const val BOX = 4
private const val LEFT = 0
private const val TOP = 1
private const val RIGHT = 2
private const val BOTTOM = 3

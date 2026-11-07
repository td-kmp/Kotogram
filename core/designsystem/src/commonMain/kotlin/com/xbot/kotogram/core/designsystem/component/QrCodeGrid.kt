package com.xbot.kotogram.core.designsystem.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import io.github.alexzhirkevich.qrose.matrix.qr.QrEncoder
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

// Telegram's qrCodeCutout, on a grid with a module of quiet zone each side.
private const val CUTOUT_SHARE = 0.297f
private const val LOGIN_CELLS = 39
private const val LOGIN_CUTOUT = 11
private const val MAX_CUTOUT = 23

// Short of the cleared modules' edges by more than the inner corners their neighbours reach into them.
private const val CUTOUT_MARGIN = 0.3f

internal class QrCodeSpec(val level: QrErrorCorrectionLevel, val modules: Int) {
    // The logo's share of the code that clears Telegram's modules: 9 of 37.
    val cutoutShare: Float get() = (cutoutModules(modules) - 2 * CUTOUT_MARGIN) / modules
}

// M, as Telegram's; Q or H when that gives the animation's grid.
internal fun qrCodeSpec(data: String, gridModules: Int): QrCodeSpec {
    val levels = listOf(
        QrErrorCorrectionLevel.Medium to QrErrorCorrection.M,
        QrErrorCorrectionLevel.MediumHigh to QrErrorCorrection.Q,
        QrErrorCorrectionLevel.High to QrErrorCorrection.H
    )
    return levels.firstNotNullOfOrNull { (level, correction) ->
        QrCodeSpec(level, QrEncoder(correction).encode(data).width).takeIf { it.modules == gridModules }
    } ?: QrCodeSpec(QrErrorCorrectionLevel.Medium, QrEncoder(QrErrorCorrection.M).encode(data).width)
}

// Whole-pixel modules centred in the box, as Telegram's, within its inset.
internal class QrGrid(box: Size, modules: Int, inset: Float) {
    val side: Float = floor((box.minDimension - 2 * inset) / modules)
    val extent: Float = side * modules
    val left: Float = ((box.width - extent) / 2).roundToInt().toFloat()
    val top: Float = ((box.height - extent) / 2).roundToInt().toFloat()
}

internal fun Modifier.layoutOnQrGrid(modules: Int, inset: Dp): Modifier = layout { measurable, constraints ->
    val width = constraints.maxWidth
    val height = constraints.maxHeight
    val grid = QrGrid(Size(width.toFloat(), height.toFloat()), modules, inset.toPx())
    val extent = grid.extent.roundToInt()
    val placeable = measurable.measure(Constraints.fixed(extent, extent))
    layout(width, height) { placeable.place(grid.left.roundToInt(), grid.top.roundToInt()) }
}

private fun cutoutModules(modules: Int): Int {
    val cells = modules + 2
    val cutout = if (cells == LOGIN_CELLS) {
        LOGIN_CUTOUT
    } else {
        (cells * CUTOUT_SHARE).roundToInt().let { if (it % 2 == 0) it + 1 else it }
    }
    return min(MAX_CUTOUT, cutout) - 2
}

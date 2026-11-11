package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

internal object DimIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode {
        val paint = Paint()
        return PressNode(interactionSource) { pressed ->
            val alpha = lerp(1f, DIMMED_ALPHA, pressed)
            if (alpha >= 1f) {
                drawContent()
            } else {
                drawIntoCanvas { canvas ->
                    paint.alpha = alpha
                    canvas.saveLayer(Rect(Offset.Zero, size), paint)
                    drawContent()
                    canvas.restore()
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = DimIndication::class.hashCode()
}

private class PressNode(
    private val interactionSource: InteractionSource,
    private val drawPressed: ContentDrawScope.(Float) -> Unit,
) : Modifier.Node(),
    DrawModifierNode {
    private val progress = Animatable(0f)
    private var pressedAt = TimeSource.Monotonic.markNow()

    override fun onAttach() {
        coroutineScope.launch {
            val presses = mutableListOf<PressInteraction.Press>()
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        presses += interaction
                        pressedAt = TimeSource.Monotonic.markNow()
                        launch {
                            progress.snapTo(1f)
                            invalidateDraw()
                        }
                    }

                    is PressInteraction.Release -> release(presses, interaction.press)

                    is PressInteraction.Cancel -> release(presses, interaction.press)
                }
            }
        }
    }

    private fun CoroutineScope.release(
        presses: MutableList<PressInteraction.Press>,
        press: PressInteraction.Press,
    ) {
        presses -= press
        if (presses.isNotEmpty()) return
        launch {
            delay(MIN_PRESSED_MILLIS - pressedAt.elapsedNow().inWholeMilliseconds)
            progress.animateTo(0f, tween(FADE_MILLIS)) { invalidateDraw() }
        }
    }

    override fun ContentDrawScope.draw() {
        drawPressed(progress.value)
    }
}

private const val DIMMED_ALPHA = 0.33f
private const val MIN_PRESSED_MILLIS = 100L
private const val FADE_MILLIS = 250

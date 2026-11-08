package com.xbot.kotogram.core.designsystem.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// The framework's activity_open_* and activity_close_* animations, and for the back gesture its
// DefaultCrossActivityBackAnimation, which a ContentTransform only approaches: there the entering screen shrinks
// with the closing one and grows back, a transition runs one way.
@Suppress("MagicNumber")
internal fun <S> androidNavTransitions(density: Density): NavTransitions<S> {
    val shift = with(density) { 96.dp.roundToPx() }
    val margin = with(density) { 8.dp.roundToPx() }
    val slide = tween<IntOffset>(DURATION, easing = FastOutExtraSlowIn)
    return NavTransitions(
        push = {
            ContentTransform(
                fadeIn(tween(83, delayMillis = 50, easing = LinearEasing)) +
                    slideInHorizontally(slide) { shift },
                slideOutHorizontally(slide) { -shift }
            )
        },
        pop = {
            ContentTransform(
                slideInHorizontally(slide) { -shift },
                fadeOut(tween(83, delayMillis = 35, easing = LinearEasing)) +
                    slideOutHorizontally(slide) { shift }
            )
        },
        predictivePop = { edge ->
            val linear = tween<IntOffset>(DURATION, easing = LinearEasing)
            ContentTransform(
                slideInHorizontally(linear) { -shift },
                scaleOut(tween(DURATION, easing = LinearEasing), targetScale = MAX_SCALE) +
                    // Shrunk against the edge it's pulled from, then off by 96dp as the back commits.
                    slideOutHorizontally(linear) { width ->
                        val toEdge = if (edge == EDGE_RIGHT) 0 else (width * (1 - MAX_SCALE) / 2).roundToInt() - margin
                        toEdge + shift
                    } +
                    fadeOut(
                        keyframes {
                            durationMillis = DURATION
                            1f at DURATION * 4 / 5
                        }
                    )
            )
        }
    )
}

private const val DURATION = 450
private const val MAX_SCALE = 0.9f

// fast_out_extra_slow_in is a path; Material 3 approaches it with this curve
// (MotionTokens.EasingEmphasizedCubicBezier).
private val FastOutExtraSlowIn = CubicBezierEasing(0.2f, 0f, 0f, 1f)

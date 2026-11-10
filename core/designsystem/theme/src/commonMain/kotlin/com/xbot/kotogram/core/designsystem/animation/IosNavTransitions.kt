package com.xbot.kotogram.core.designsystem.animation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut

// Copied from Compose Multiplatform's NavDisplay.ios.kt (Apache-2.0).
@OptIn(ExperimentalAnimationApi::class)
internal fun <S> iosNavTransitions(): NavTransitions<S> = NavTransitions(
    push = {
        ContentTransform(
            slideIntoContainer(SlideDirection.Start, tween(DURATION, easing = IosEasing)),
            slideOutOfContainer(
                SlideDirection.Start,
                tween(
                    DURATION,
                    easing = IosEasing,
                ),
                targetOffset = {
                    it /
                        4
                },
            ) +
                veilOut(tween(DURATION, easing = IosEasing)),
        )
    },
    pop = {
        ContentTransform(
            slideIntoContainer(
                SlideDirection.End,
                tween(
                    DURATION,
                    easing = IosEasing,
                ),
                initialOffset = {
                    it /
                        4
                },
            ) +
                unveilIn(tween(DURATION, easing = IosEasing)),
            slideOutOfContainer(SlideDirection.End, tween(DURATION, easing = IosEasing)),
        )
    },
    predictivePop = { edge ->
        val towards = if (edge == EDGE_RIGHT) SlideDirection.Left else SlideDirection.Right
        ContentTransform(
            slideIntoContainer(towards, tween(DURATION, easing = LinearEasing), initialOffset = {
                it /
                    4
            }) +
                unveilIn(tween(DURATION, easing = LinearEasing)),
            slideOutOfContainer(towards, tween(DURATION, easing = LinearEasing)),
        )
    },
)

private const val DURATION = 500

@Suppress("MagicNumber")
private val IosEasing = CubicBezierEasing(0.2833f, 0.99f, 0.31833f, 0.99f)

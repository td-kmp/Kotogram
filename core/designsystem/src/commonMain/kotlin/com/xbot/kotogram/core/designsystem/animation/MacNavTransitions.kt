package com.xbot.kotogram.core.designsystem.animation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut

// Telegram for macOS's NavigationViewController: the new screen over the whole width, the old one a third of it
// aside under a backdrop that darkens in a third of the time, 0.4 s. Its curve is a CASpringAnimation (mass 3,
// stiffness 1000, damping 500) cut at the duration; this ease-out only approaches it.
@Suppress("MagicNumber")
@OptIn(ExperimentalAnimationApi::class)
internal fun <S> macNavTransitions(): NavTransitions<S> = NavTransitions(
    push = {
        ContentTransform(
            slideIntoContainer(SlideDirection.Start, tween(DURATION, easing = SpringLike)),
            slideOutOfContainer(SlideDirection.Start, tween(DURATION, easing = SpringLike), targetOffset = { it / 3 }) +
                veilOut(tween(DURATION / 3, easing = SpringLike))
        )
    },
    pop = {
        ContentTransform(
            slideIntoContainer(SlideDirection.End, tween(DURATION, easing = SpringLike), initialOffset = { it / 3 }) +
                unveilIn(tween(DURATION / 3, easing = SpringLike)),
            slideOutOfContainer(SlideDirection.End, tween(DURATION, easing = SpringLike))
        )
    },
    // A trackpad swipe, followed by the finger as Telegram does.
    predictivePop = {
        ContentTransform(
            slideIntoContainer(SlideDirection.End, tween(DURATION, easing = LinearEasing), initialOffset = { it / 3 }) +
                unveilIn(tween(DURATION, easing = LinearEasing)),
            slideOutOfContainer(SlideDirection.End, tween(DURATION, easing = LinearEasing))
        )
    }
)

private const val DURATION = 400

@Suppress("MagicNumber")
private val SpringLike = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)

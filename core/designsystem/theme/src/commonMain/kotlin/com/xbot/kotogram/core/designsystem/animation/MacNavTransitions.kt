package com.xbot.kotogram.core.designsystem.animation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.LayoutDirection

@Suppress("MagicNumber")
@OptIn(ExperimentalAnimationApi::class)
internal fun <S> macNavTransitions(layoutDirection: LayoutDirection): NavTransitions<S> {
    // Workaround: AnimatedContent keeps a clipped screen at the left in RTL too, so there screens slide side by side.
    val covers = layoutDirection == LayoutDirection.Ltr
    val aside: (Int) -> Int = if (covers) {
        { it / 3 }
    } else {
        { it }
    }
    fun covered(easing: Easing): ExitTransition =
        if (covers) shrinkHorizontally(move(easing), shrinkTowards = Alignment.Start) { 0 } else ExitTransition.None
    fun uncovered(easing: Easing): EnterTransition =
        if (covers) expandHorizontally(move(easing), expandFrom = Alignment.Start) { 0 } else EnterTransition.None
    return NavTransitions(
        push = {
            ContentTransform(
                slideIntoContainer(SlideDirection.Start, move(SpringLike)),
                slideOutOfContainer(SlideDirection.Start, move(SpringLike), aside) +
                    covered(SpringLike) +
                    veilOut(tween(DURATION / 3, easing = SpringLike)),
            )
        },
        pop = {
            ContentTransform(
                slideIntoContainer(SlideDirection.End, move(SpringLike), aside) +
                    uncovered(SpringLike) +
                    unveilIn(tween(DURATION / 3, easing = SpringLike)),
                slideOutOfContainer(SlideDirection.End, move(SpringLike)),
            )
        },
        predictivePop = {
            ContentTransform(
                slideIntoContainer(SlideDirection.End, move(LinearEasing), aside) +
                    uncovered(LinearEasing) +
                    unveilIn(tween(DURATION, easing = LinearEasing)),
                slideOutOfContainer(SlideDirection.End, move(LinearEasing)),
            )
        },
    )
}

private fun <T> move(easing: Easing): FiniteAnimationSpec<T> = tween(DURATION, easing = easing)

private const val DURATION = 400

@Suppress("MagicNumber")
private val SpringLike = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)

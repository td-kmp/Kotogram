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

// Copied from Compose Multiplatform's NavDisplay.ios.kt (Apache-2.0), with the screen underneath clipped as on macOS.
@Suppress("MagicNumber")
@OptIn(ExperimentalAnimationApi::class)
internal fun <S> iosNavTransitions(layoutDirection: LayoutDirection): NavTransitions<S> {
    // Workaround: AnimatedContent keeps a clipped screen at the left in RTL too, so there screens slide side by side.
    val covers = layoutDirection == LayoutDirection.Ltr
    val aside: (Int) -> Int = if (covers) {
        { it / 4 }
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
                slideIntoContainer(SlideDirection.Start, move(IosEasing)),
                slideOutOfContainer(SlideDirection.Start, move(IosEasing), aside) +
                    covered(IosEasing) +
                    veilOut(move(IosEasing)),
            )
        },
        pop = {
            ContentTransform(
                slideIntoContainer(SlideDirection.End, move(IosEasing), aside) +
                    uncovered(IosEasing) +
                    unveilIn(move(IosEasing)),
                slideOutOfContainer(SlideDirection.End, move(IosEasing)),
            )
        },
        predictivePop = { edge ->
            val towards = if (edge == EDGE_RIGHT) SlideDirection.Left else SlideDirection.Right
            val revealed = if (edge == EDGE_RIGHT) EnterTransition.None else uncovered(LinearEasing)
            ContentTransform(
                slideIntoContainer(towards, move(LinearEasing), aside) +
                    revealed +
                    unveilIn(move(LinearEasing)),
                slideOutOfContainer(towards, move(LinearEasing)),
            )
        },
    )
}

private fun <T> move(easing: Easing): FiniteAnimationSpec<T> = tween(DURATION, easing = easing)

private const val DURATION = 500

@Suppress("MagicNumber")
private val IosEasing = CubicBezierEasing(0.2833f, 0.99f, 0.31833f, 0.99f)

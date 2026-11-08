package com.xbot.kotogram.core.designsystem.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

@Immutable
class NavTransitions<S> internal constructor(
    val push: AnimatedContentTransitionScope<S>.() -> ContentTransform,
    val pop: AnimatedContentTransitionScope<S>.() -> ContentTransform,
    val predictivePop: AnimatedContentTransitionScope<S>.(swipeEdge: Int) -> ContentTransform
)

@Composable
fun <S> rememberNavTransitions(): NavTransitions<S> = platformNavTransitions()

@Composable
internal expect fun <S> platformNavTransitions(): NavTransitions<S>

// NavigationEvent.EDGE_RIGHT; the design system has no navigationevent dependency.
internal const val EDGE_RIGHT = 1

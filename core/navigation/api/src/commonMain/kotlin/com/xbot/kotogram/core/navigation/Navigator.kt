package com.xbot.kotogram.core.navigation

import androidx.compose.runtime.compositionLocalOf

interface Navigator {
    val backStack: List<NavKey>
    val currentDestination: NavKey?

    fun navigate(key: NavKey)

    fun navigateBack()

    fun setRoot(key: NavKey)
}

val LocalNavigator = compositionLocalOf<Navigator> { error("No navigator provided") }

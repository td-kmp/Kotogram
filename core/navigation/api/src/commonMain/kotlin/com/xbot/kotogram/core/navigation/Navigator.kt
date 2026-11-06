package com.xbot.kotogram.core.navigation

import androidx.compose.runtime.compositionLocalOf

interface Navigator {
    val backStack: List<NavKey>
    val currentDestination: NavKey?

    fun navigate(key: NavKey)

    fun navigateBack()

    // A stack that already starts at the key is kept, so a restored back stack survives.
    fun setRoot(key: NavKey)
}

val LocalNavigator = compositionLocalOf<Navigator> { error("No navigator provided") }

package com.xbot.kotogram.core.navigation

import androidx.compose.runtime.compositionLocalOf

/** The app-wide navigator. Only navigation entries use it; screens receive callbacks. */
interface Navigator {
    val backStack: List<NavKey>
    val currentDestination: NavKey?

    fun navigate(key: NavKey)

    fun navigateBack()

    /**
     * Makes [key] the root of the back stack, for session switches (auth → main, account switch).
     * A stack that already starts at [key] is kept, so a restored back stack survives.
     */
    fun setRoot(key: NavKey)
}

val LocalNavigator = compositionLocalOf<Navigator> { error("No navigator provided") }

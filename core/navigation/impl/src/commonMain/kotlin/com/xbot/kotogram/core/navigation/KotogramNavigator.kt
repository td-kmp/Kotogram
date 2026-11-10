package com.xbot.kotogram.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule

@Composable
fun rememberNavigator(
    startRoute: NavKey,
    serializersModule: SerializersModule,
): Navigator {
    val backStack = rememberSerializable(
        configuration = SavedStateConfiguration { this.serializersModule = serializersModule },
        serializer = NavBackStackSerializer(PolymorphicSerializer(NavKey::class)),
    ) {
        NavBackStack(startRoute)
    }
    return remember(backStack) { KotogramNavigator(backStack) }
}

private class KotogramNavigator(
    private val stack: NavBackStack<NavKey>,
) : Navigator {
    override val backStack: List<NavKey>
        get() = stack

    override val currentDestination: NavKey?
        get() = stack.lastOrNull()

    override fun navigate(key: NavKey) {
        stack.add(key)
    }

    override fun navigateBack() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    override fun setRoot(key: NavKey) {
        if (stack.firstOrNull() == key) return
        stack.clear()
        stack.add(key)
    }
}

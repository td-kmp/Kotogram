package com.xbot.kotogram.shared.root

import androidx.navigation3.runtime.EntryProviderScope
import com.xbot.kotogram.core.navigation.FeatureNavigation
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.feature.auth.impl.navigation.AuthNavigation
import com.xbot.kotogram.feature.chats.impl.navigation.ChatsNavigation
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

/** The app shell's own routes. */
private object ShellNavigation : FeatureNavigation {
    override fun PolymorphicModuleBuilder<NavKey>.routes() {
        subclass(SplashRoute::class)
    }

    override fun EntryProviderScope<NavKey>.entries() {
        entry<SplashRoute> { SplashScreen() }
    }
}

/** A new feature is one more line here. */
internal val appNavigation: List<FeatureNavigation> =
    listOf(ShellNavigation, AuthNavigation, ChatsNavigation)

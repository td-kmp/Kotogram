package com.xbot.kotogram.core.navigation

import androidx.navigation3.runtime.EntryProviderScope
import kotlinx.serialization.modules.PolymorphicModuleBuilder

/** One feature's part of the navigation graph. `shared` lists the features, in one place. */
interface FeatureNavigation {
    /** Every key of the feature that can reach the back stack, which is saved. */
    fun PolymorphicModuleBuilder<NavKey>.routes()

    fun EntryProviderScope<NavKey>.entries()
}

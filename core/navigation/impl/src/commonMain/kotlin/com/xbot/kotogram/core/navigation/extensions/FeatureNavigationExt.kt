package com.xbot.kotogram.core.navigation.extensions

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import com.xbot.kotogram.core.navigation.FeatureNavigation
import com.xbot.kotogram.core.navigation.NavKey
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

fun List<FeatureNavigation>.navSerializersModule(): SerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        forEach { feature -> with(feature) { routes() } }
    }
}

fun List<FeatureNavigation>.navEntryProvider(): (NavKey) -> NavEntry<NavKey> = entryProvider {
    forEach { feature -> with(feature) { entries() } }
}

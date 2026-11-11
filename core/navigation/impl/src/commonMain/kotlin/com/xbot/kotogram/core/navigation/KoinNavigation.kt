package com.xbot.kotogram.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.getKoin

@Composable
fun koinNavSerializersModule(): SerializersModule {
    val koin = getKoin()
    return remember(koin) {
        val configs = koin.getAll<PolymorphicSerializerConfig<NavKey>>()
        SerializersModule {
            polymorphic(NavKey::class) { configs.forEach { it.configure(this) } }
        }
    }
}

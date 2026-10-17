package com.xbot.kotogram.core.navigation

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.qualifier.named

/** One feature's subtypes of a polymorphic base, collected from Koin to build the app's SerializersModule. */
fun interface PolymorphicSerializerConfig<T : Any> {
    fun configure(builder: PolymorphicModuleBuilder<T>)
}

/**
 * Registers subtypes of [T] for saving the back stack: `polymorphic<NavKey> { subclass(ChatRoute::class) }`.
 * Each call gets its own qualifier, so every feature can contribute one.
 */
@OptIn(ExperimentalUuidApi::class)
inline fun <reified T : Any> Module.polymorphic(
    crossinline builder: PolymorphicModuleBuilder<T>.() -> Unit
): KoinDefinition<PolymorphicSerializerConfig<T>> = single(named(Uuid.random().toString())) {
    PolymorphicSerializerConfig { it.builder() }
}

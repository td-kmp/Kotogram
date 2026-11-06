package com.xbot.kotogram.core.navigation

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.qualifier.named

fun interface PolymorphicSerializerConfig<T : Any> {
    fun configure(builder: PolymorphicModuleBuilder<T>)
}

@OptIn(ExperimentalUuidApi::class)
inline fun <reified T : Any> Module.polymorphic(
    crossinline builder: PolymorphicModuleBuilder<T>.() -> Unit
): KoinDefinition<PolymorphicSerializerConfig<T>> =
    // A qualifier of its own per call, so every feature can contribute one.
    single(named(Uuid.random().toString())) { PolymorphicSerializerConfig { it.builder() } }

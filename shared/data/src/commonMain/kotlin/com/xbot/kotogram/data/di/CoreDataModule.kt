package com.xbot.kotogram.data.di

import com.xbot.kotogram.data.tdlib.TdSessionManager
import org.koin.core.module.Module
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

val coreDataModule: Module = module {
    includes(platformDataModule)
    single { TdSessionManager(get()).apply { start() } } withOptions {
        onClose { it?.close() }
    }
}

/** Provides the platform's [com.xbot.kotogram.data.tdlib.TdlibEnvironment]. */
internal expect val platformDataModule: Module

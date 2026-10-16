package com.xbot.kotogram.data.di

import com.xbot.kotogram.data.tdlib.TelegramClient
import org.koin.core.module.Module
import org.koin.dsl.module

val coreDataModule: Module = module {
    includes(platformDataModule)
    single { TelegramClient(get()).apply { start() } }
}

/** Provides the platform's [com.xbot.kotogram.data.tdlib.TdlibEnvironment]. */
internal expect val platformDataModule: Module

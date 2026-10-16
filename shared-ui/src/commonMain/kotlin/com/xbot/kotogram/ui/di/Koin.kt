package com.xbot.kotogram.ui.di

import com.xbot.kotogram.data.di.coreDataModule
import com.xbot.kotogram.data.tdlib.TelegramClient
import com.xbot.kotogram.feature.auth.data.di.authDataModule
import com.xbot.kotogram.feature.auth.presentation.di.authPresentationModule
import com.xbot.kotogram.ui.root.RootViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

private val appModule = module {
    viewModelOf(::RootViewModel)
}

/** Starts Koin once per process; platform entry points add their own bits through [declaration]. */
fun initKoin(declaration: KoinAppDeclaration = {}) {
    startKoin {
        declaration()
        modules(coreDataModule, authDataModule, authPresentationModule, appModule)
    }
}

/** Shuts TDLib down cleanly; desktop calls it from a JVM shutdown hook. */
suspend fun closeTelegramClient() {
    KoinPlatform.getKoinOrNull()?.getOrNull<TelegramClient>()?.close()
}

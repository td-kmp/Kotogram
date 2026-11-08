package com.xbot.kotogram.shared.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import com.xbot.kotogram.core.account.AccountManager
import com.xbot.kotogram.core.account.di.accountsModule
import com.xbot.kotogram.feature.auth.data.di.authDataModule
import com.xbot.kotogram.feature.auth.impl.di.authImplModule
import com.xbot.kotogram.feature.chats.data.di.chatsDataModule
import com.xbot.kotogram.feature.chats.impl.di.chatsImplModule
import com.xbot.kotogram.feature.language.data.di.languageDataModule
import com.xbot.kotogram.feature.language.impl.di.languageImplModule
import com.xbot.kotogram.feature.media.data.di.mediaDataModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.includes
import org.koin.mp.KoinPlatform

fun initKoin(config: KoinConfiguration? = null) {
    val koin = startKoin {
        logger(KermitKoinLogger(Logger.withTag("Koin")))
        modules(
            accountsModule,
            authDataModule,
            authImplModule,
            chatsDataModule,
            chatsImplModule,
            languageDataModule,
            languageImplModule,
            mediaDataModule
        )
        includes(config)
    }.koin
    koin.get<AccountManager>().start()
}

suspend fun closeAccounts() {
    KoinPlatform.getKoin().get<AccountManager>().closeAll()
}

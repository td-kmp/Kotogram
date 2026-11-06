package com.xbot.kotogram.core.account.di

import com.xbot.kotogram.core.account.AccountManager
import com.xbot.kotogram.core.account.AccountRegistryStore
import com.xbot.kotogram.core.account.AccountStorage
import com.xbot.kotogram.core.account.DatabaseKeys
import com.xbot.kotogram.core.account.TdlibConfig
import io.xbot.tdlib.TdLib
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val appScope = named("appScope")

val accountsModule = module {
    single(appScope) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    // The library's own object: TdReceiver runs one loop per TdLib, and a wrapper made twice steals its events.
    single<TdLib> { platformTdLib() }
    single<TdlibConfig> { platformTdlibConfig() }
    single<AccountStorage> { platformAccountStorage() }
    single<AccountRegistryStore> { platformAccountRegistryStore() }
    single<DatabaseKeys> { platformDatabaseKeys() }
    single {
        AccountManager(
            appScope = get(appScope),
            tdLib = get(),
            config = get(),
            store = get(),
            storage = get(),
            keys = get(),
            koin = getKoin()
        )
    }
}

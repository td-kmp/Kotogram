package com.xbot.kotogram.core.account.di

import com.xbot.kotogram.core.account.AccountManager
import com.xbot.kotogram.core.account.AccountRegistryStore
import com.xbot.kotogram.core.account.AccountStorage
import com.xbot.kotogram.core.account.AppClient
import com.xbot.kotogram.core.account.AppCoroutineScope
import com.xbot.kotogram.core.account.AppStorage
import com.xbot.kotogram.core.account.DataStoreAccountRegistryStore
import com.xbot.kotogram.core.account.DatabaseKeys
import com.xbot.kotogram.core.account.DefaultAppClient
import com.xbot.kotogram.core.account.TdlibConfig
import io.xbot.tdlib.TdLib
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val accountsModule = module {
    single(AppCoroutineScope) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single<TdLib> { platformTdLib() }
    single<TdlibConfig> { platformTdlibConfig() }
    single<AccountStorage> { platformAccountStorage() }
    single<AppStorage> { platformAppStorage() }
    single<AccountRegistryStore> { DataStoreAccountRegistryStore(get<AppStorage>().preferences) }
    single<DatabaseKeys> { platformDatabaseKeys() }
    single {
        AccountManager(
            appScope = get(AppCoroutineScope),
            tdLib = get(),
            config = get(),
            store = get(),
            storage = get(),
            keys = get(),
            koin = getKoin()
        )
    }
    single<AppClient> { DefaultAppClient(accounts = get()) }
}

package com.xbot.kotogram.feature.connection.data.di

import com.xbot.kotogram.core.account.AccountScope
import com.xbot.kotogram.core.account.AccountUpdates
import com.xbot.kotogram.core.account.AppCoroutineScope
import com.xbot.kotogram.core.account.TdlibOptions
import com.xbot.kotogram.feature.connection.data.account.NetworkTypeUpdates
import com.xbot.kotogram.feature.connection.data.account.OnlineOptions
import com.xbot.kotogram.feature.connection.data.repository.DefaultConnectionRepository
import com.xbot.kotogram.feature.connection.data.repository.DefaultConnectionStateRepository
import com.xbot.kotogram.feature.connection.data.store.ConnectionStore
import com.xbot.kotogram.feature.connection.domain.repository.ConnectionRepository
import com.xbot.kotogram.feature.connection.domain.repository.ConnectionStateRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.bind
import org.koin.dsl.module

val connectionDataModule = module {
    single {
        DefaultConnectionRepository(
            appScope = get(AppCoroutineScope),
            network = platformNetworkMonitor(),
        )
    } bind ConnectionRepository::class
    scope(AccountScope) {
        scopedOf(::ConnectionStore) { bind<AccountUpdates>() }
        scopedOf(::DefaultConnectionStateRepository) { bind<ConnectionStateRepository>() }
        scopedOf(::OnlineOptions) { bind<TdlibOptions>() }
        scopedOf(::NetworkTypeUpdates) { bind<AccountUpdates>() }
    }
}

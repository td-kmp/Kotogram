package com.xbot.kotogram.core.tdlib

import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.core.tdlib.account.AccountLifetime
import com.xbot.kotogram.core.tdlib.account.AccountRepository
import com.xbot.kotogram.core.tdlib.account.AccountSessions
import com.xbot.kotogram.core.tdlib.account.AccountStorage
import com.xbot.kotogram.core.tdlib.account.DefaultAccountRepository
import com.xbot.kotogram.core.tdlib.client.ClientHolder
import com.xbot.kotogram.core.tdlib.client.DefaultClientHolder
import com.xbot.kotogram.core.tdlib.client.ManagedClientHolder
import com.xbot.kotogram.core.tdlib.client.UpdateLane
import com.xbot.kotogram.core.tdlib.di.KoinAccountLifetime
import com.xbot.kotogram.core.tdlib.di.KoinAccountSessions
import com.xbot.kotogram.core.tdlib.di.platformAccountStorage
import com.xbot.kotogram.core.tdlib.environment.TdlibEnvironment
import com.xbot.kotogram.core.tdlib.store.AccountStore
import com.xbot.kotogram.core.tdlib.store.DefaultAccountStore
import com.xbot.kotogram.core.tdlib.store.MutableAccountStore
import io.xbot.tdlib.NativeTdLib
import io.xbot.tdlib.TdLib
import java.nio.file.Files
import java.nio.file.Path
import org.koin.core.KoinApplication
import org.koin.dsl.binds
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** The production wiring of core:tdlib, on a temporary directory instead of the app's data. */
internal fun testKoin(
    root: Path = Files.createTempDirectory("kotogram-tdlib"),
    tdLib: TdLib = NativeTdLib
): KoinApplication = koinApplication {
    modules(
        module {
            single<TdlibEnvironment> { TestEnvironment(root.toString()) }
            single<TdLib> { tdLib }
            single { UpdateLane() }
            single<AccountStorage> { platformAccountStorage(get()) }
            single<AccountSessions> { KoinAccountSessions(this) }
            single<AccountRepository> { DefaultAccountRepository(get(), get()) }
            scope<AccountId> {
                scoped<AccountLifetime> { KoinAccountLifetime(this) }
                scoped { DefaultAccountStore() } binds
                    arrayOf(AccountStore::class, MutableAccountStore::class)
                // The scope's source, as sessions open it; compile safety can't see it.
                scoped {
                    DefaultClientHolder(
                        checkNotNull(getOrNull<AccountId>()),
                        get(),
                        get(),
                        get(),
                        get(),
                        get()
                    )
                } binds arrayOf(ClientHolder::class, ManagedClientHolder::class)
            }
        }
    )
}

private class TestEnvironment(root: String) : TdlibEnvironment {
    override val databaseDirectory = "$root/db"
    override val filesDirectory = "$root/files"
    override val deviceModel = "kotogram test"
    override val systemVersion = "jvm"
    override val systemLanguageCode = "en"
    override val applicationVersion = "test"
}

package com.xbot.kotogram.core.data

import com.xbot.kotogram.core.data.account.AccountStorage
import com.xbot.kotogram.core.data.account.DefaultAccountRepository
import com.xbot.kotogram.core.data.account.PlatformAccountStorage
import com.xbot.kotogram.core.data.client.ClientHolder
import com.xbot.kotogram.core.data.client.DefaultClientHolder
import com.xbot.kotogram.core.data.client.TdLibProvider
import com.xbot.kotogram.core.data.client.UpdateLane
import com.xbot.kotogram.core.data.environment.TdlibEnvironment
import com.xbot.kotogram.core.data.store.AccountStore
import com.xbot.kotogram.core.data.store.DefaultAccountStore
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.core.domain.repository.AccountRepository
import io.xbot.tdlib.NativeTdLib
import io.xbot.tdlib.TdLib
import java.nio.file.Files
import java.nio.file.Path
import org.koin.core.KoinApplication
import org.koin.dsl.bind
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
            single<TdLibProvider> {
                object : TdLibProvider {
                    override val tdLib = tdLib
                }
            }
            single { UpdateLane() }
            single<AccountStorage> { PlatformAccountStorage(get()) }
            single<AccountRepository> { DefaultAccountRepository(get(), this) }
            scope<AccountId> {
                scoped { DefaultAccountStore() } bind AccountStore::class
                // The scope's source, as DefaultAccountRepository opens it; compile safety can't see it.
                scoped<ClientHolder> {
                    DefaultClientHolder(
                        checkNotNull(getOrNull<AccountId>()),
                        get(),
                        get(),
                        get(),
                        get(),
                        this
                    )
                }
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

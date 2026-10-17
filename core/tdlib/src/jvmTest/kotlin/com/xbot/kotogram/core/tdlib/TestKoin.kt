package com.xbot.kotogram.core.tdlib

import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.core.domain.repository.AccountRepository
import java.nio.file.Files
import java.nio.file.Path
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** The production wiring of core:tdlib, on a temporary directory instead of the app's data. */
internal fun testKoin(root: Path = Files.createTempDirectory("kotogram-tdlib")): KoinApplication =
    koinApplication {
        modules(
            module {
                single<TdlibEnvironment> { TestEnvironment(root.toString()) }
                single<AccountRepository> { DefaultAccountRepository(get(), this) }
                scope<AccountId> {
                    // The scope's source, as DefaultAccountRepository opens it; compile safety can't see it.
                    scoped<ClientHolder> {
                        DefaultClientHolder(checkNotNull(getOrNull<AccountId>()), get(), this)
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

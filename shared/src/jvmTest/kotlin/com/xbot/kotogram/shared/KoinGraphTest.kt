package com.xbot.kotogram.shared

import com.xbot.kotogram.core.data.account.accountScope
import com.xbot.kotogram.core.data.environment.TdlibEnvironment
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.core.domain.repository.AccountRepository
import com.xbot.kotogram.shared.di.initKoin
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

class KoinGraphTest {
    @Test
    fun theProductionGraphOpensAnAccount() = runTest {
        withContext(Dispatchers.Default) {
            withTimeout(45.seconds) {
                val environment = TemporaryEnvironment()
                initKoin(
                    koinConfiguration {
                        modules(module { single<TdlibEnvironment> { environment } })
                    }
                )
                val koin = KoinPlatform.getKoin()
                try {
                    // Checked first: the repository would otherwise open the real accounts on this machine.
                    assertSame(environment, koin.get<TdlibEnvironment>())
                    // Nullable requests aren't validated in a test compilation: architecture.md §2.5.
                    val accounts = checkNotNull(koin.getOrNull<AccountRepository>())
                    val active = accounts.active.first {
                        it?.authorizationState == AuthorizationStateWaitPhoneNumber
                    }
                    assertEquals(AccountId(1), active?.id)
                    assertNotNull(koin.accountScope(AccountId(1)))
                } finally {
                    stopKoin()
                }
            }
        }
    }
}

private class TemporaryEnvironment : TdlibEnvironment {
    private val root = Files.createTempDirectory("kotogram-graph")
    override val databaseDirectory = "$root/db"
    override val filesDirectory = "$root/files"
    override val deviceModel = "kotogram test"
    override val systemVersion = "jvm"
    override val systemLanguageCode = "en"
    override val applicationVersion = "test"
}

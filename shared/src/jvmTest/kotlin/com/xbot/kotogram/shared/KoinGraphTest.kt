package com.xbot.kotogram.shared

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import com.xbot.kotogram.core.account.AccountId
import com.xbot.kotogram.core.account.AccountManager
import com.xbot.kotogram.core.account.AccountRegistry
import com.xbot.kotogram.core.account.AccountRegistryStore
import com.xbot.kotogram.core.account.AccountScope
import com.xbot.kotogram.core.account.AccountStorage
import com.xbot.kotogram.core.account.TdlibConfig
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.feature.auth.api.AuthRoute
import com.xbot.kotogram.feature.chats.api.ChatListRoute
import com.xbot.kotogram.feature.chats.api.ChatRoute
import com.xbot.kotogram.shared.di.initKoin
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.koin.compose.navigation3.EntryProviderInstaller
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

class KoinGraphTest {
    @OptIn(KoinInternalApi::class, KoinExperimentalAPI::class)
    @Test
    fun everyDefinitionOfAnAccountResolves() = runTest {
        withContext(Dispatchers.Default) {
            withTimeout(45.seconds) {
                initKoin(
                    koinConfiguration {
                        modules(
                            module {
                                single<TdlibConfig> { testConfig }
                                single<AccountStorage> { TemporaryAccountStorage() }
                                single<AccountRegistryStore> { InMemoryAccountRegistryStore() }
                            }
                        )
                    }
                )
                val koin = KoinPlatform.getKoin()
                val manager = koin.get<AccountManager>()
                try {
                    val session = checkNotNull(manager.shown.first { it.active != null }.active)
                    assertEquals(AccountId(1), session.id)
                    session.authorizationState.first { it == AuthorizationStateWaitPhoneNumber }

                    val definitions = koin.instanceRegistry.instances.values
                        .map { it.beanDefinition }
                        .filter { it.scopeQualifier == AccountScope }
                        .distinct()
                    assertTrue(definitions.size > 2, "only the declared client and context")
                    definitions.forEach { definition ->
                        // The one parameter there is: ChatViewModel's chat id.
                        session.koinScope.get<Any>(definition.primaryType) { parametersOf(1L) }
                    }

                    val installers = session.koinScope.getAll<EntryProviderInstaller>()
                    val screens = entryProvider<NavKey> {
                        @Suppress("UNCHECKED_CAST")
                        installers.forEach { (this as EntryProviderScope<Any>).it() }
                    }
                    listOf(AuthRoute, ChatListRoute, ChatRoute(chatId = 1)).forEach {
                        screens(it)
                    }
                } finally {
                    manager.closeAll()
                    stopKoin()
                }
            }
        }
    }
}

private val testConfig = TdlibConfig(
    apiId = 1,
    apiHash = "test",
    useTestDc = true,
    systemLanguageCode = "en",
    deviceModel = "kotogram test",
    systemVersion = "jvm",
    applicationVersion = "test"
)

private class TemporaryAccountStorage : AccountStorage {
    private val root = Files.createTempDirectory("kotogram-graph")

    override fun databaseDirectory(id: AccountId) = "$root/${id.value}/db"

    override fun filesDirectory(id: AccountId) = "$root/${id.value}/files"

    override val languagePackDatabase = "$root/langpack"

    override suspend fun delete(id: AccountId) = Unit
}

private class InMemoryAccountRegistryStore : AccountRegistryStore {
    private var saved = AccountRegistry()

    override suspend fun load(): AccountRegistry = saved

    override suspend fun save(registry: AccountRegistry) {
        saved = registry
    }
}

package com.xbot.kotogram.core.tdlib

import com.xbot.kotogram.core.domain.model.AccountId
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateClosed
import io.xbot.tdlib.api.SetTdlibParameters
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.client.TdClient
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped
import org.koin.core.scope.Scope as KoinScope
import org.koin.core.scope.ScopeCallback

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalAtomicApi::class)
@Scope(AccountId::class)
@Scoped
internal class DefaultClientHolder(
    // The scope's source: DefaultAccountRepository opens every account scope with its id.
    @Provided private val accountId: AccountId,
    private val environment: TdlibEnvironment,
    koinScope: KoinScope
) : ClientHolder {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val isClosed = AtomicBoolean(false)

    // The live instance and the ones after it, in order; a late collector starts at the live one.
    private val instances = MutableSharedFlow<TdClient>(replay = 1)

    // `null` only until the first instance reports; a restart goes on from Closed to the next instance's
    // first state, so the account never looks unstarted in between.
    override val authorizationState: StateFlow<AuthorizationState?> = instances
        .flatMapLatest { it.authorizationState.filterNotNull() }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override val updates: Flow<Update> = instances.flatMapConcat { it.updates }

    init {
        koinScope.registerCallback(
            object : ScopeCallback {
                override fun onScopeClose(scope: KoinScope) = close()
            }
        )
        scope.launch {
            // TDLib decides when an instance ends; the holder only starts the next one.
            while (!isClosed.load()) {
                start().authorizationState.first { it is AuthorizationStateClosed }
            }
        }
    }

    override suspend fun <R : TdObject> send(function: TdFunction<R>): R {
        check(!isClosed.load()) { "The client of account ${accountId.value} is closed" }
        return instances.first().send(function)
    }

    private suspend fun start(): TdClient {
        TdlibLog.install()
        val client = TdClient.create()
        instances.emit(client)
        // The parameters are the first request: no round trip through authorizationStateWaitTdlibParameters.
        scope.launch { runCatching { client.send(parameters()) } }
        return client
    }

    /** Blocks until TDLib has released the instance: the JVM crashes if it exits during td_receive. */
    private fun close() {
        if (isClosed.exchange(true)) return
        runBlocking {
            withTimeoutOrNull(CLOSE_TIMEOUT) { instances.replayCache.lastOrNull()?.close() }
        }
        scope.cancel()
    }

    private fun parameters(): SetTdlibParameters {
        check(TelegramApiConfig.API_ID != 0 && TelegramApiConfig.API_HASH.isNotEmpty()) {
            "Telegram API credentials are missing: set telegram.api.id and telegram.api.hash in local.properties"
        }
        return SetTdlibParameters(
            useTestDc = TelegramApiConfig.USE_TEST_DC,
            databaseDirectory = environment.databaseDirectoryOf(accountId),
            filesDirectory = environment.filesDirectoryOf(accountId),
            useFileDatabase = true,
            useChatInfoDatabase = true,
            useMessageDatabase = true,
            useSecretChats = true,
            apiId = TelegramApiConfig.API_ID,
            apiHash = TelegramApiConfig.API_HASH,
            systemLanguageCode = environment.systemLanguageCode,
            deviceModel = environment.deviceModel,
            systemVersion = environment.systemVersion,
            applicationVersion = environment.applicationVersion
        )
    }

    private companion object {
        val CLOSE_TIMEOUT = 3.seconds
    }
}

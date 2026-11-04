package com.xbot.kotogram.core.data.client

import co.touchlab.kermit.Logger
import com.xbot.kotogram.core.data.TelegramApiConfig
import com.xbot.kotogram.core.data.environment.TdlibEnvironment
import com.xbot.kotogram.core.data.environment.databaseDirectoryOf
import com.xbot.kotogram.core.data.environment.filesDirectoryOf
import com.xbot.kotogram.core.data.store.DefaultAccountStore
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.core.domain.model.isLoginStep
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateClosed
import io.xbot.tdlib.api.AuthorizationStateLoggingOut
import io.xbot.tdlib.api.Destroy
import io.xbot.tdlib.api.Error
import io.xbot.tdlib.api.SetTdlibParameters
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateAuthorizationState
import io.xbot.tdlib.client.TdClient
import io.xbot.tdlib.client.TdException
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
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
    private val store: DefaultAccountStore,
    tdLibProvider: TdLibProvider,
    lane: UpdateLane,
    koinScope: KoinScope
) : ClientHolder {
    private val tdLib = tdLibProvider.tdLib
    private val scope = CoroutineScope(SupervisorJob() + lane.dispatcher)
    private val logger = Logger.withTag("Account ${accountId.value}")
    private val isShutDown = AtomicBoolean(false)
    private val isDestroying = AtomicBoolean(false)

    private val instance = MutableStateFlow<TdClient?>(null)

    private val mutableLifecycle = MutableStateFlow(AccountLifecycle.Live)
    override val lifecycle: StateFlow<AccountLifecycle> = mutableLifecycle.asStateFlow()

    // `null` only until the first instance reports; a restart goes on from Closed to the next instance's
    // first state, so the account never looks unstarted in between.
    override val authorizationState: StateFlow<AuthorizationState?> = instance
        .filterNotNull()
        .flatMapLatest { it.authorizationState.filterNotNull() }
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val instances = scope.launch { runInstances() }

    init {
        koinScope.registerCallback(
            object : ScopeCallback {
                // Normal teardown has already shut down; this is stopKoin() at exit.
                override fun onScopeClose(scope: KoinScope) {
                    if (!isShutDown.load()) runAtExit { shutdown() }
                }
            }
        )
    }

    private suspend fun runInstances() {
        TdlibLog.install(tdLib)
        while (!isShutDown.load()) {
            runInstance(TdClient.create(tdLib))
            if (mutableLifecycle.value == AccountLifecycle.LoggingOut || isDestroying.load()) {
                mutableLifecycle.value = AccountLifecycle.Ended
                return
            }
        }
    }

    // The restart loop and the only collector are one coroutine, so instances can't interleave.
    private suspend fun runInstance(client: TdClient) {
        var previous: AuthorizationState? = null
        try {
            supervisorScope {
                val setup = launch(Dispatchers.Default, CoroutineStart.LAZY) { setUp(client) }
                client.updates
                    // Publishing here means no caller can send before this subscription exists:
                    // TDLib starts on the first request, and `updates` has no replay.
                    .onSubscription {
                        instance.value = client
                        setup.start()
                    }
                    .transformWhile { update ->
                        emit(update)
                        !update.closesInstance()
                    }
                    .collect { update ->
                        if (update is UpdateAuthorizationState) {
                            val state = update.authorizationState
                            if (state is AuthorizationStateLoggingOut && !previous.isLoginStep) {
                                mutableLifecycle.value = AccountLifecycle.LoggingOut
                            }
                            previous = state
                        }
                        store.apply(update)
                    }
                setup.cancel()
            }
        } finally {
            // Only a cancelled iteration gets here with a live instance. Never leak one.
            withContext(NonCancellable) { client.close() }
        }
    }

    private suspend fun setUp(client: TdClient) {
        try {
            client.send(parameters())
        } catch (e: TdException) {
            logger.e(e) { "setTdlibParameters failed" }
        }
    }

    override suspend fun <R : TdObject> send(function: TdFunction<R>): R {
        if (isShutDown.load() || mutableLifecycle.value == AccountLifecycle.Ended) {
            throw TdException(
                Error(code = ACCOUNT_CLOSED, message = "Account ${accountId.value} is closed")
            )
        }
        return instance.filterNotNull().first().send(function)
    }

    override suspend fun destroy() {
        if (isDestroying.exchange(true)) return
        try {
            send(Destroy)
        } catch (e: TdException) {
            logger.w(e) { "destroy failed" }
        }
    }

    override suspend fun shutdown(): Boolean {
        isShutDown.store(true)
        // Cancelling first means no instance can start after the directories are deleted. The JVM
        // crashes if it exits during td_receive, so this waits until TDLib has released the instance.
        val released = withTimeoutOrNull(CLOSE_TIMEOUT) { instances.cancelAndJoin() } != null
        scope.cancel()
        return released
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
        const val ACCOUNT_CLOSED = 500
    }
}

private fun Update.closesInstance() =
    this is UpdateAuthorizationState && authorizationState is AuthorizationStateClosed

package com.xbot.kotogram.data.tdlib

import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateClosed
import io.xbot.tdlib.api.AuthorizationStateWaitTdlibParameters
import io.xbot.tdlib.api.GetAuthorizationState
import io.xbot.tdlib.api.SetLogVerbosityLevel
import io.xbot.tdlib.api.SetTdlibParameters
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The app's single TDLib instance. Answers setTdlibParameters itself and starts a fresh instance
 * after the current one closes (log out), so [authorizationState] continues across instances.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TelegramClient(private val environment: TdlibEnvironment) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private val client = MutableStateFlow<TdClient?>(null)
    private var isClosed = false

    val authorizationState: Flow<AuthorizationState> =
        client.filterNotNull().flatMapLatest { it.authorizationState.filterNotNull() }

    val updates: Flow<Update> = client.filterNotNull().flatMapLatest { it.updates }

    /** Creates the TDLib instance if needed; TDLib only starts after the first request. */
    fun start() {
        scope.launch { activeClient() }
    }

    /**
     * Closes TDLib for good. Call before the process exits: the JVM crashes in native code if it
     * shuts down while TDLib's receive thread is still inside td_receive.
     */
    suspend fun close() {
        val current = mutex.withLock {
            isClosed = true
            client.value
        }
        current?.close()
    }

    /** @throws io.xbot.tdlib.client.TdException when TDLib answers with an error. */
    suspend fun <R : TdObject> send(function: TdFunction<R>): R = activeClient().send(function)

    private suspend fun activeClient(): TdClient = mutex.withLock {
        client.value?.takeIf { it.authorizationState.value !is AuthorizationStateClosed }
            ?: create()
    }

    private suspend fun create(): TdClient {
        runCatching { TdClient.execute(SetLogVerbosityLevel(newVerbosityLevel = LOG_VERBOSITY)) }
        val created = TdClient.create()
        client.value = created
        scope.launch {
            created.authorizationState.filterNotNull().collect { state ->
                when (state) {
                    AuthorizationStateWaitTdlibParameters -> created.send(tdlibParameters())
                    AuthorizationStateClosed -> if (!mutex.withLock { isClosed }) activeClient()
                    else -> Unit
                }
            }
        }
        scope.launch { runCatching { created.send(GetAuthorizationState) } }
        return created
    }

    private fun tdlibParameters(): SetTdlibParameters {
        check(TelegramApiConfig.API_ID != 0 && TelegramApiConfig.API_HASH.isNotEmpty()) {
            "Telegram API credentials are missing: set telegram.api.id and telegram.api.hash in local.properties"
        }
        return SetTdlibParameters(
            databaseDirectory = environment.databaseDirectory,
            filesDirectory = environment.filesDirectory,
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
        const val LOG_VERBOSITY = 1
    }
}

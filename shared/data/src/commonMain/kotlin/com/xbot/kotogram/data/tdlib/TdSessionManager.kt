package com.xbot.kotogram.data.tdlib

import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateClosed
import io.xbot.tdlib.api.AuthorizationStateWaitTdlibParameters
import io.xbot.tdlib.api.GetAuthorizationState
import io.xbot.tdlib.api.SetLogVerbosityLevel
import io.xbot.tdlib.api.SetTdlibParameters
import io.xbot.tdlib.client.TdClient
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Keeps one live [TdClient] for the app: creates it lazily, answers setTdlibParameters, and
 * replaces it after it closes (log out), since a closed TDLib instance can't be reused.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TdSessionManager(private val environment: TdlibEnvironment) : AutoCloseable {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private val current = MutableStateFlow<TdClient?>(null)
    private var isClosed = false

    /** Authorization state of whichever instance is live; continues across replacements. */
    val authorizationState: Flow<AuthorizationState> =
        current.filterNotNull().flatMapLatest { it.authorizationState.filterNotNull() }

    /** Creates the TDLib instance ahead of the first request; TDLib only starts after one. */
    fun start() {
        scope.launch { client() }
    }

    /** The live instance, created if there is none or the previous one has closed. */
    suspend fun client(): TdClient = mutex.withLock {
        check(!isClosed) { "TdSessionManager is closed" }
        current.value?.takeIf { it.authorizationState.value !is AuthorizationStateClosed }
            ?: create()
    }

    /**
     * Closes TDLib for good, blocking until it has released the instance. Koin calls this on
     * stopKoin(); the JVM crashes in native code if it exits while TDLib is still in td_receive.
     */
    override fun close() {
        runBlocking {
            val client = mutex.withLock {
                isClosed = true
                current.value
            }
            withTimeoutOrNull(CLOSE_TIMEOUT) { client?.close() }
        }
        scope.cancel()
    }

    private suspend fun create(): TdClient {
        runCatching { TdClient.execute(SetLogVerbosityLevel(newVerbosityLevel = LOG_VERBOSITY)) }
        val created = TdClient.create()
        current.value = created
        scope.launch {
            created.authorizationState.filterNotNull().collect { state ->
                when (state) {
                    AuthorizationStateWaitTdlibParameters -> created.send(tdlibParameters())
                    AuthorizationStateClosed -> if (!mutex.withLock { isClosed }) client()
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
        val CLOSE_TIMEOUT = 3.seconds
    }
}

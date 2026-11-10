package com.xbot.kotogram.core.account

import co.touchlab.kermit.Logger
import com.xbot.kotogram.core.account.extensions.isLoginStep
import com.xbot.kotogram.core.account.extensions.sendCatching
import io.xbot.tdlib.TdLib
import io.xbot.tdlib.api.AuthorizationStateClosed
import io.xbot.tdlib.api.AuthorizationStateLoggingOut
import io.xbot.tdlib.api.AuthorizationStateReady
import io.xbot.tdlib.api.AuthorizationStateWaitTdlibParameters
import io.xbot.tdlib.api.GetMe
import io.xbot.tdlib.api.GetOption
import io.xbot.tdlib.api.OptionValue
import io.xbot.tdlib.api.SetOption
import io.xbot.tdlib.api.UpdateAuthorizationState
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.Koin
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

data class ShownAccounts(
    val active: AccountSession? = null,
    val adding: AccountSession? = null,
)

@Suppress("TooManyFunctions")
class AccountManager(
    private val appScope: CoroutineScope,
    private val tdLib: TdLib,
    private val config: TdlibConfig,
    private val store: AccountRegistryStore,
    private val storage: AccountStorage,
    private val keys: DatabaseKeys,
    private val koin: Koin,
    private val maxAccounts: Int = MAX_ACCOUNTS,
) {
    private val logger = Logger.withTag("AccountManager")
    private val mutex = Mutex()

    private var sessions = emptyMap<AccountId, AccountSession>()
    private var addingId: AccountId? = null

    private val mutableRegistry = MutableStateFlow(AccountRegistry())
    val registry: StateFlow<AccountRegistry> = mutableRegistry.asStateFlow()

    private val mutableShown = MutableStateFlow(ShownAccounts())
    val shown: StateFlow<ShownAccounts> = mutableShown.asStateFlow()

    private val mutableAccounts = MutableStateFlow(emptyList<AccountSession>())
    val accounts: StateFlow<List<AccountSession>> = mutableAccounts.asStateFlow()

    fun start() {
        appScope.launch {
            TdlibLog.install(tdLib)
            mutex.withLock {
                val restored = withoutUnfinishedSignIns(store.load())
                val next = when {
                    restored.accounts.isEmpty() -> restored.withNewAccount().let {
                        it.copy(active = it.newestId)
                    }

                    restored.active == null -> restored.copy(active = restored.accounts.first().id)

                    else -> restored
                }
                setRegistry(next)
                next.accounts.forEach { open(it.id) }
                publish()
            }
        }
    }

    fun canAdd(): Boolean = registry.value.signedIn.size < maxAccounts

    suspend fun add(): AccountSession? = mutex.withLock {
        addingId?.let { return@withLock sessions[it] }
        if (!canAdd()) return@withLock null
        val next = registry.value.withNewAccount()
        setRegistry(next)
        addingId = next.newestId
        open(next.newestId).also { publish() }
    }

    fun cancelAdding() {
        appScope.launch {
            val id = mutex.withLock { addingId } ?: return@launch
            endSignIn(id, restart = false)
        }
    }

    // Workaround: TDLib leaves QR login only through a log out, so sign-in goes on in a new account.
    fun restartSignIn(id: AccountId) {
        appScope.launch { endSignIn(id, restart = true) }
    }

    suspend fun switchTo(id: AccountId) = mutex.withLock {
        if (registry.value.entry(id)?.userId != null) {
            setRegistry(registry.value.copy(active = id))
            publish()
        }
    }

    suspend fun logOut(id: AccountId) {
        mutex.withLock { sessions[id] }?.logOut()
    }

    // Workaround: TDLib crashes the JVM if it exits during td_receive.
    suspend fun closeAll() {
        val open = mutex.withLock { sessions.values.toList() }
        open.forEach { it.closingByApp = true }
        withTimeoutOrNull(CLOSE_TIMEOUT) {
            coroutineScope { open.forEach { launch { it.client.close() } } }
        }
    }

    private suspend fun endSignIn(
        id: AccountId,
        restart: Boolean,
    ) {
        val session = mutex.withLock {
            val session = sessions[id]?.takeUnless { it.closingByApp } ?: return
            session.closingByApp = true
            if (!restart && addingId == id) {
                addingId = null
                publish()
            }
            session
        }
        session.logOut()
        session.authorizationState.first { it is AuthorizationStateClosed }
        remove(id, restart)
    }

    private suspend fun open(id: AccountId): AccountSession {
        val client = TdClient.create(tdLib)
        val coroutines = CoroutineScope(
            appScope.coroutineContext +
                SupervisorJob(appScope.coroutineContext[Job]) +
                CoroutineName("account-${id.value}"),
        )
        val koinScope = koin.createScope("account-${id.value}", AccountScope)
        koinScope.declare(client)
        koinScope.declare(AccountContext(id, client, coroutines))
        val session = AccountSession(id, koinScope, client, coroutines)

        koinScope.getAll<AccountUpdates>().forEach { it.start(coroutines, client) }
        coroutines.launch(start = CoroutineStart.UNDISPATCHED) { setUp(session) }
        coroutines.launch(start = CoroutineStart.UNDISPATCHED) { follow(session) }

        sessions = sessions + (id to session)
        client.sendCatching(GetOption(name = "version")).onLeft {
            logger.e { "Account ${id.value} didn't start: ${it.message}" }
        }
        return session
    }

    private suspend fun setUp(session: AccountSession) {
        session.authorizationState.first { it is AuthorizationStateWaitTdlibParameters }
        val key = keys.getOrCreate(session.id)
        if (key == null) {
            onDatabaseKeyLost(session)
            return
        }
        coroutineScope {
            val optionsSent = CompletableDeferred<Unit>()
            launch { sendOptions(session.client, session.koinScope.getAll(), optionsSent) }
            optionsSent.await()
            session.client.sendCatching(config.parameters(session.id, storage, key)).onLeft {
                logger.e { "setTdlibParameters failed for ${session.id.value}: ${it.message}" }
            }
        }
    }

    private suspend fun sendOptions(
        client: TdClient,
        owners: List<TdlibOptions>,
        firstSent: CompletableDeferred<Unit>,
    ) {
        if (owners.isEmpty()) {
            firstSent.complete(Unit)
            return
        }
        coroutineScope {
            var sent = emptyMap<String, OptionValue>()
            combine(owners.map { it.options }) { it.reduce(Map<String, OptionValue>::plus) }.collect { options ->
                options.filter { (name, value) -> sent[name] != value }.forEach { (name, value) ->
                    // Workaround: awaiting hangs, TDLib answers after setTdlibParameters; send keeps the order.
                    launch(start = CoroutineStart.UNDISPATCHED) {
                        client.sendCatching(SetOption(name = name, value = value)).onLeft {
                            logger.e { "Option $name wasn't set: ${it.message}" }
                        }
                    }
                }
                sent = options
                firstSent.complete(Unit)
            }
        }
    }

    // Workaround: only a signed-in account starts over, or a broken Keystore would loop.
    private suspend fun onDatabaseKeyLost(session: AccountSession) {
        logger.e { "Account ${session.id.value} lost its database key" }
        if (registry.value.entry(session.id)?.userId == null) return
        session.closingByApp = true
        appScope.launch {
            session.client.close()
            remove(session.id)
        }
    }

    // Workaround: read from updates, a StateFlow could drop LoggingOut on its way to Closed.
    private suspend fun follow(session: AccountSession) {
        var isLoggingOut = false
        session.client.updatesOf<UpdateAuthorizationState>()
            .map { it.authorizationState }
            .transformWhile {
                emit(it)
                it !is AuthorizationStateClosed
            }
            .collect { state ->
                when (state) {
                    is AuthorizationStateReady -> onReady(session)
                    is AuthorizationStateLoggingOut -> isLoggingOut = true
                    is AuthorizationStateClosed -> onClosed(session, isLoggingOut)
                    else -> Unit
                }
            }
    }

    private fun onClosed(
        session: AccountSession,
        isLoggingOut: Boolean,
    ) {
        when {
            session.closingByApp -> Unit
            isLoggingOut -> appScope.launch { remove(session.id) }
            else -> logger.e { "Account ${session.id.value} closed without a log out" }
        }
    }

    private suspend fun onReady(session: AccountSession) {
        val me = session.client.sendCatching(GetMe).fold(
            ifLeft = {
                logger.e { "getMe failed for ${session.id.value}: ${it.message}" }
                return
            },
            ifRight = { it },
        )
        val isDuplicate = mutex.withLock {
            if (sessions[session.id] !== session) return
            val current = registry.value
            val existing = current.accounts.firstOrNull {
                it.id != session.id && it.userId == me.id
            }
            val wasAdding = addingId == session.id
            if (wasAdding) addingId = null
            val signedIn = current.update(session.id) { it.copy(userId = me.id) }
            setRegistry(
                when {
                    existing != null -> current.copy(active = existing.id)
                    wasAdding -> signedIn.copy(active = session.id)
                    else -> signedIn
                },
            )
            publish()
            existing != null
        }
        if (isDuplicate) session.logOut()
    }

    private suspend fun remove(
        id: AccountId,
        restart: Boolean = false,
    ) {
        var replacement: AccountSession? = null
        val session = mutex.withLock {
            val session = sessions[id]
            sessions = sessions - id
            val wasAdding = addingId == id
            if (wasAdding) addingId = null
            val rest = registry.value.without(id)
            if (rest.active == null || restart) {
                val next = rest.withNewAccount()
                val isAboveActive = restart && wasAdding && rest.active != null
                setRegistry(if (isAboveActive) next else next.copy(active = next.newestId))
                if (isAboveActive) addingId = next.newestId
                replacement = open(next.newestId)
            } else {
                setRegistry(rest)
                publish()
            }
            session
        }
        replacement?.let {
            awaitFirstScreen(it)
            mutex.withLock { publish() }
        }
        session?.let(::retire)
        deleteData(id)
    }

    private suspend fun awaitFirstScreen(session: AccountSession) {
        withTimeoutOrNull(REPLACE_TIMEOUT) {
            session.authorizationState.first { it.isLoginStep || it == AuthorizationStateReady }
        }
    }

    private fun retire(session: AccountSession) {
        session.cancelWork()
        // Workaround: a screen still on display reads the scope until its next frame, and a closed one throws.
        appScope.launch {
            delay(SCOPE_CLOSE_DELAY)
            session.koinScope.close()
        }
    }

    private suspend fun withoutUnfinishedSignIns(loaded: AccountRegistry): AccountRegistry {
        val (kept, unfinished) = loaded.accounts.partition {
            it.userId != null || it.id == loaded.active
        }
        unfinished.forEach { deleteData(it.id) }
        return loaded.copy(accounts = kept)
    }

    private suspend fun deleteData(id: AccountId) {
        try {
            storage.delete(id)
            keys.delete(id)
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.e(e) { "Can't delete the data of account ${id.value}" }
        }
    }

    private suspend fun setRegistry(next: AccountRegistry) {
        if (next == mutableRegistry.value) return
        mutableRegistry.value = next
        store.save(next)
    }

    private fun publish() {
        val current = registry.value
        mutableShown.value = ShownAccounts(
            active = current.active?.let { sessions[it] },
            adding = addingId?.let { sessions[it] },
        )
        mutableAccounts.value = current.signedIn.mapNotNull { sessions[it.id] }
    }

    private companion object {
        val CLOSE_TIMEOUT = 3.seconds
        val SCOPE_CLOSE_DELAY = 1.seconds
        val REPLACE_TIMEOUT = 5.seconds
    }
}

private const val MAX_ACCOUNTS = 3

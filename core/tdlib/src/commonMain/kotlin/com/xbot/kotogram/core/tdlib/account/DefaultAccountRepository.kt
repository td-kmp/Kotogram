package com.xbot.kotogram.core.tdlib.account

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.xbot.kotogram.core.model.Account
import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.core.model.extensions.isLoginStep
import com.xbot.kotogram.core.tdlib.client.AccountLifecycle
import com.xbot.kotogram.core.tdlib.extensions.account
import com.xbot.kotogram.core.tdlib.extensions.getValue
import io.xbot.tdlib.api.AuthorizationStateReady
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.annotation.Singleton

/**
 * An account is one login's life: one TDLib database, one session, one id that never repeats. A real
 * log out ends it, and if none is left a new one shows the login. Only the active account may be
 * unauthorized: a non-active one at a login step is destroyed. See docs/design/accounts.md.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
internal class DefaultAccountRepository(
    private val storage: AccountStorage,
    private val sessions: AccountSessions
) : AccountRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private val pointers = AccountPointerKeeper(storage)

    /** `null` until the accounts on disk are opened. */
    private val ids = MutableStateFlow<List<AccountId>?>(null)
    private val shownId = MutableStateFlow<AccountId?>(null)

    override val accounts: StateFlow<List<Account>> = ids
        .filterNotNull()
        .flatMapLatest { ids ->
            if (ids.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(ids.map { sessions.getValue(it).account }) { it.toList() }
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val active: StateFlow<Account?> = combine(accounts, pointers.active) { accounts, id ->
        accounts.firstOrNull { it.id == id }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    init {
        scope.launch { load() }
    }

    override suspend fun addAccount(): Either<AccountError, AccountId> = mutex.withLock {
        Either.Right(createAccount())
    }

    override suspend fun switchTo(id: AccountId): Either<AccountError, Unit> = either {
        val account = ensureNotNull(accounts.value.firstOrNull { it.id == id }) {
            AccountError.UnknownAccount
        }
        ensure(account.authorizationState == AuthorizationStateReady) {
            AccountError.NotAuthorized
        }
        mutex.withLock { pointers.activate(id) }
    }

    override fun onAccountShown(id: AccountId?) {
        shownId.value = id
    }

    private suspend fun load() = mutex.withLock {
        val stored = storage.accounts()
        val active = pointers.restore(stored)
        if (active == null) {
            ids.value = emptyList()
            createAccount()
        } else {
            open(active)
            (stored - active).forEach(::open)
            ids.value = stored
        }
    }

    private suspend fun createAccount(): AccountId {
        val id = pointers.nextId()
        storage.create(id)
        // Active before it opens, so its watcher never sees it as a non-active account at a login step.
        pointers.activate(id)
        open(id)
        ids.value = ids.value.orEmpty() + id
        return id
    }

    private fun open(id: AccountId) {
        val session = sessions.open(id)
        // The lifecycle is sticky and the other condition level-triggered, so conflation can't lose either.
        scope.launch {
            val destroyIfAbandoned = launch {
                combine(session.client.authorizationState, pointers.active) { state, active ->
                    id != active && state.isLoginStep
                }.first { it }
                session.client.destroy()
            }
            session.client.lifecycle.first { it != AccountLifecycle.Live }
            handOver(id)
            session.client.lifecycle.first { it == AccountLifecycle.Ended }
            destroyIfAbandoned.cancel()
            end(session)
        }
    }

    /** The UI moves first, when there is an account to move to. */
    private suspend fun handOver(from: AccountId) = mutex.withLock {
        if (pointers.active.value == from) successorOf(from)?.let { pointers.activate(it) }
    }

    private suspend fun end(session: AccountSession) {
        val id = session.id
        mutex.withLock {
            if (pointers.active.value == id) {
                val next = successorOf(id)
                if (next != null) pointers.activate(next) else createAccount()
            }
            ids.value = ids.value.orEmpty() - id
        }
        // A stopped activity doesn't recompose until it returns, so this can't wait forever.
        withTimeoutOrNull(UI_TIMEOUT) { shownId.first { it != id } }
        val released = session.client.shutdown()
        session.close()
        // If TDLib didn't confirm, the directories stay and the next start deals with them.
        if (released) storage.delete(id)
    }

    private fun successorOf(id: AccountId): AccountId? {
        val candidates = ids.value.orEmpty().filter {
            it != id && !sessions.getValue(it).client.authorizationState.value.isLoginStep
        }
        return pointers.previous?.takeIf { it in candidates } ?: candidates.firstOrNull()
    }

    private companion object {
        val UI_TIMEOUT = 2.seconds
    }
}

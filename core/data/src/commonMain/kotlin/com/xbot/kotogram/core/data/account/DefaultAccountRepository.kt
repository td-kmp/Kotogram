package com.xbot.kotogram.core.data.account

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.xbot.kotogram.core.data.client.AccountLifecycle
import com.xbot.kotogram.core.data.client.ClientHolder
import com.xbot.kotogram.core.data.environment.directoryName
import com.xbot.kotogram.core.domain.model.Account
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.core.domain.model.isLoginStep
import com.xbot.kotogram.core.domain.repository.AccountError
import com.xbot.kotogram.core.domain.repository.AccountRepository
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
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

/**
 * An account is one login's life: one TDLib database, one Koin scope, one id that never repeats. A real
 * log out ends it, and if none is left a new one shows the login. Only the active account may be
 * unauthorized: a non-active one at a login step is destroyed. See docs/design/accounts.md.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
internal class DefaultAccountRepository(private val storage: AccountStorage, koinScope: Scope) :
    AccountRepository {

    private val koin = koinScope.getKoin()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()

    /** `null` until the accounts on disk are opened. */
    private val ids = MutableStateFlow<List<AccountId>?>(null)
    private val activeId = MutableStateFlow<AccountId?>(null)
    private val shownId = MutableStateFlow<AccountId?>(null)
    private var previousId: AccountId? = null
    private var lastId = 0

    override val accounts: StateFlow<List<Account>> = ids
        .filterNotNull()
        .flatMapLatest { ids ->
            if (ids.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(ids.map(koin::accountOf)) { it.toList() }
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val active: StateFlow<Account?> = combine(accounts, activeId) { accounts, id ->
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
        mutex.withLock { setActive(id) }
    }

    override fun onAccountShown(id: AccountId?) {
        shownId.value = id
    }

    private suspend fun load() = mutex.withLock {
        val stored = storage.accounts()
        val saved = storage.pointers()
        lastId = maxOf(saved.lastId, stored.maxOfOrNull { it.value } ?: 0)
        previousId = saved.previous
        val active = saved.active?.takeIf { it in stored } ?: stored.firstOrNull()
        if (active == null) {
            ids.value = emptyList()
            createAccount()
        } else {
            activeId.value = active
            open(active)
            (stored - active).forEach(::open)
            ids.value = stored
        }
    }

    private suspend fun createAccount(): AccountId {
        val id = AccountId(++lastId)
        storage.create(id)
        // Active before it opens, so its watcher never sees it as a non-active account at a login step.
        setActive(id)
        open(id)
        ids.value = ids.value.orEmpty() + id
        return id
    }

    private fun open(id: AccountId) {
        // Starts the account's TDLib instance.
        val holder = koin.createScope(
            id.directoryName,
            named<AccountId>(),
            source = id
        ).get<ClientHolder>()
        // The lifecycle is sticky and the other condition level-triggered, so conflation can't lose either.
        scope.launch {
            val destroyIfAbandoned = launch {
                combine(holder.authorizationState, activeId) { state, active ->
                    id != active && state.isLoginStep
                }.first { it }
                holder.destroy()
            }
            holder.lifecycle.first { it != AccountLifecycle.Live }
            handOver(id)
            holder.lifecycle.first { it == AccountLifecycle.Ended }
            destroyIfAbandoned.cancel()
            end(id, holder)
        }
    }

    /** The UI moves first, when there is an account to move to. */
    private suspend fun handOver(from: AccountId) = mutex.withLock {
        if (activeId.value == from) successorOf(from)?.let { setActive(it) }
    }

    private suspend fun end(id: AccountId, holder: ClientHolder) {
        mutex.withLock {
            if (activeId.value == id) {
                val next = successorOf(id)
                if (next != null) setActive(next) else createAccount()
            }
            ids.value = ids.value.orEmpty() - id
        }
        // A stopped activity doesn't recompose until it returns, so this can't wait forever.
        withTimeoutOrNull(UI_TIMEOUT) { shownId.first { it != id } }
        val released = holder.shutdown()
        koin.accountScope(id)?.close()
        // If TDLib didn't confirm, the directories stay and the next start deals with them.
        if (released) storage.delete(id)
    }

    private fun successorOf(id: AccountId): AccountId? {
        val candidates = ids.value.orEmpty().filter {
            it != id && !koin.holderOf(it).authorizationState.value.isLoginStep
        }
        return previousId?.takeIf { it in candidates } ?: candidates.firstOrNull()
    }

    private suspend fun setActive(id: AccountId) {
        val current = activeId.value
        if (current != id) {
            previousId = current ?: previousId
            activeId.value = id
        }
        storage.savePointers(AccountPointers(active = id, previous = previousId, lastId = lastId))
    }

    private companion object {
        val UI_TIMEOUT = 2.seconds
    }
}

package com.xbot.kotogram.core.tdlib

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.xbot.kotogram.core.domain.model.Account
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.core.domain.model.AuthorizationState
import com.xbot.kotogram.core.domain.repository.AccountError
import com.xbot.kotogram.core.domain.repository.AccountRepository
import com.xbot.kotogram.core.tdlib.mapper.toDomain
import io.xbot.tdlib.api.GetOption
import io.xbot.tdlib.api.OptionValueInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import okio.Path.Companion.toPath
import org.koin.core.annotation.Singleton
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

/**
 * Accounts are the TDLib databases under [TdlibEnvironment.databaseDirectory], and their state is
 * TDLib's. The only own data is which account is active. Two rules keep the list tidy, both read from
 * TDLib states:
 * 1. the active account logging out after `Ready` hands the UI to another authorized account;
 * 2. a non-active account waiting for a phone number or a QR confirmation is removed. That is an account
 *    TDLib wiped on log out, an "add account" the user switched away from, or a leftover from an earlier run.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
internal class DefaultAccountRepository(
    private val environment: TdlibEnvironment,
    koinScope: Scope
) : AccountRepository {

    private val koin = koinScope.getKoin()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()

    /** `null` until the accounts on disk are opened. */
    private val ids = MutableStateFlow<List<AccountId>?>(null)
    private val activeId = MutableStateFlow<AccountId?>(null)

    private val preferences = PreferenceDataStoreFactory.createWithPath {
        "${environment.databaseDirectory}/accounts.preferences_pb".toPath()
    }

    override val accounts: StateFlow<List<Account>> = ids
        .filterNotNull()
        .flatMapLatest { ids ->
            if (ids.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(ids.map(::accountOf)) {
                    it.toList()
                }
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val active: StateFlow<Account?> = combine(accounts, activeId) { accounts, id ->
        accounts.firstOrNull { it.id == id }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    init {
        scope.launch {
            load()
            keepTidy()
        }
    }

    override suspend fun addAccount(): Either<AccountError, AccountId> = mutex.withLock {
        val current = ids.value.orEmpty()
        val id = AccountId((current.maxOfOrNull { it.value } ?: 0) + 1)
        open(id)
        // Active before it is listed, so rule 2 never sees it as a non-active unauthorized account.
        setActive(id)
        ids.value = current + id
        Either.Right(id)
    }

    override suspend fun switchTo(id: AccountId): Either<AccountError, Unit> = either {
        val account =
            ensureNotNull(
                accounts.value.firstOrNull {
                    it.id == id
                }
            ) { AccountError.UnknownAccount }
        ensure(account.authorizationState == AuthorizationState.Ready) {
            AccountError.NotAuthorized
        }
        mutex.withLock { setActive(id) }
    }

    private suspend fun load() = mutex.withLock {
        SystemFileSystem.createDirectories(Path(environment.databaseDirectory))
        val stored = SystemFileSystem.list(Path(environment.databaseDirectory))
            .map { it.name }
            .filter { it.startsWith(ACCOUNT_PREFIX) }
            .mapNotNull { it.removePrefix(ACCOUNT_PREFIX).toIntOrNull() }
            .sorted()
            .map(::AccountId)
        val all = stored.ifEmpty { listOf(AccountId(1)) }
        all.forEach(::open)
        val saved = preferences.data.first()[ACTIVE]?.let(::AccountId)
        setActive(saved?.takeIf { it in all } ?: all.first())
        ids.value = all
    }

    private suspend fun keepTidy() {
        var previous = emptyMap<AccountId, AuthorizationState?>()
        combine(accounts, activeId, ::Pair).collect { (accounts, activeId) ->
            for (account in accounts) {
                val state = account.authorizationState
                when {
                    account.id == activeId &&
                        state == AuthorizationState.LoggingOut &&
                        previous[account.id] == AuthorizationState.Ready -> handOver(account.id)

                    account.id != activeId &&
                        (
                            state == AuthorizationState.WaitPhoneNumber ||
                                state is AuthorizationState.WaitOtherDeviceConfirmation
                            ) ->
                        remove(account.id)
                }
            }
            previous = accounts.associate { it.id to it.authorizationState }
        }
    }

    private suspend fun handOver(from: AccountId) = mutex.withLock {
        val next = accounts.value.firstOrNull {
            it.id != from &&
                it.authorizationState == AuthorizationState.Ready
        }
        if (next != null) setActive(next.id)
    }

    private suspend fun remove(id: AccountId) = mutex.withLock {
        ids.value = ids.value.orEmpty() - id
        koin.accountScope(id)?.close()
        deleteRecursively(Path(environment.databaseDirectoryOf(id)))
        deleteRecursively(Path(environment.filesDirectoryOf(id)))
    }

    private fun open(id: AccountId) {
        SystemFileSystem.createDirectories(Path(environment.databaseDirectoryOf(id)))
        // Starts the account's TDLib instance; the scope is closed by remove() or by stopKoin().
        koin.createScope(id.directoryName, named<AccountId>(), source = id).get<ClientHolder>()
    }

    private fun accountOf(id: AccountId): Flow<Account> {
        val holder = checkNotNull(koin.accountScope(id)) {
            "Account ${id.value} is not open"
        }.get<ClientHolder>()
        return holder.authorizationState.mapLatest { state ->
            val domain = state?.toDomain()
            val userId = if (domain == AuthorizationState.Ready) holder.myId() else null
            Account(id = id, authorizationState = domain, userId = userId)
        }
    }

    private suspend fun ClientHolder.myId(): Long? =
        runCatching { (send(GetOption(name = "my_id")) as? OptionValueInteger)?.value }.getOrNull()

    private suspend fun setActive(id: AccountId) {
        activeId.value = id
        preferences.edit { it[ACTIVE] = id.value }
    }

    private fun deleteRecursively(path: Path) {
        if (!SystemFileSystem.exists(path)) return
        if (SystemFileSystem.metadataOrNull(path)?.isDirectory == true) {
            SystemFileSystem.list(path).forEach(::deleteRecursively)
        }
        SystemFileSystem.delete(path, mustExist = false)
    }

    private companion object {
        val ACTIVE = intPreferencesKey("active_account")
    }
}

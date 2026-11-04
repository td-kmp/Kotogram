package com.xbot.kotogram.core.tdlib

import co.touchlab.kermit.Logger
import com.xbot.kotogram.core.domain.model.AccountId
import io.xbot.tdlib.api.AuthorizationStateClosed
import io.xbot.tdlib.api.OptionValue
import io.xbot.tdlib.api.OptionValueEmpty
import io.xbot.tdlib.api.OptionValueInteger
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateAuthorizationState
import io.xbot.tdlib.api.UpdateOption
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

/**
 * One account's TDLib state as an immutable snapshot, a pure fold of its updates. A plain class:
 * StateFlow compares with equals, and generated equals over DTOs is deep with a ByteArray hole.
 */
class AccountState internal constructor(
    val options: PersistentMap<String, OptionValue> = persistentMapOf()
) {
    val myId: Long? get() = (options[MY_ID] as? OptionValueInteger)?.value

    internal companion object {
        val Empty = AccountState()
        private const val MY_ID = "my_id"
    }
}

interface AccountStore {
    /** Never misses anything. */
    val state: StateFlow<AccountState>

    /** Each update after it was applied to [state], from the moment of subscription only. */
    val updates: SharedFlow<Update>
}

@Scope(AccountId::class)
@Scoped
internal class DefaultAccountStore : AccountStore {
    private val mutableState = MutableStateFlow(AccountState.Empty)
    override val state: StateFlow<AccountState> = mutableState.asStateFlow()

    // Without a buffer, tryEmit drops the update whenever a subscriber isn't parked at that instant.
    private val mutableUpdates = MutableSharedFlow<Update>(extraBufferCapacity = Int.MAX_VALUE)
    override val updates: SharedFlow<Update> = mutableUpdates.asSharedFlow()

    /** Only the holder's collector calls this, in TDLib's order. */
    fun apply(update: Update) {
        // A reducer bug must not take the account down.
        runCatching { mutableState.value.reduce(update) }
            .onSuccess { mutableState.value = it }
            .onFailure { Logger.e(it) { "Can't apply ${update::class.simpleName}" } }
        mutableUpdates.tryEmit(update)
    }
}

internal fun AccountState.reduce(update: Update): AccountState = when (update) {
    is UpdateAuthorizationState ->
        if (update.authorizationState is AuthorizationStateClosed) AccountState.Empty else this

    is UpdateOption -> AccountState(
        options = if (update.value is OptionValueEmpty) {
            options.removing(update.name)
        } else {
            options.putting(update.name, update.value)
        }
    )

    else -> this
}

package com.xbot.kotogram.core.data.store

import co.touchlab.kermit.Logger
import com.xbot.kotogram.core.domain.model.AccountId
import io.xbot.tdlib.api.Update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

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

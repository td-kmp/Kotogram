package com.xbot.kotogram.core.tdlib.store

import co.touchlab.kermit.Logger
import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.core.tdlib.extensions.reduce
import io.xbot.tdlib.api.Update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

@Scope(AccountId::class)
@Scoped(binds = [AccountStore::class, MutableAccountStore::class])
internal class DefaultAccountStore : MutableAccountStore {
    private val mutableState = MutableStateFlow(AccountState.Empty)
    override val state: StateFlow<AccountState> = mutableState.asStateFlow()

    // Without a buffer, tryEmit drops the update whenever a subscriber isn't parked at that instant.
    private val mutableUpdates = MutableSharedFlow<Update>(extraBufferCapacity = Int.MAX_VALUE)
    override val updates: SharedFlow<Update> = mutableUpdates.asSharedFlow()

    override fun apply(update: Update) {
        // A reducer bug must not take the account down.
        runCatching { mutableState.value.reduce(update) }
            .onSuccess { mutableState.value = it }
            .onFailure { Logger.e(it) { "Can't apply ${update::class.simpleName}" } }
        mutableUpdates.tryEmit(update)
    }
}

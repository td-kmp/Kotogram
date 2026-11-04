package com.xbot.kotogram.core.tdlib.store

import io.xbot.tdlib.api.Update
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface AccountStore {
    /** Never misses anything. */
    val state: StateFlow<AccountState>

    /** Each update after it was applied to [state], from the moment of subscription only. */
    val updates: SharedFlow<Update>
}

internal interface MutableAccountStore : AccountStore {
    /** Only the client holder's collector calls this, in TDLib's order. */
    fun apply(update: Update)
}

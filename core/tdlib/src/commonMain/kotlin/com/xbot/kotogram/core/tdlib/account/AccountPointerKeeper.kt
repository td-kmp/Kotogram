package com.xbot.kotogram.core.tdlib.account

import com.xbot.kotogram.core.model.AccountId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The active account, the one before it, and the last id used: ids never repeat. */
internal class AccountPointerKeeper(private val store: AccountPointerStore) {
    private val mutableActive = MutableStateFlow<AccountId?>(null)
    val active: StateFlow<AccountId?> = mutableActive.asStateFlow()

    var previous: AccountId? = null
        private set
    private var lastId = 0

    /** Returns the account to show first, if there is one. */
    suspend fun restore(stored: List<AccountId>): AccountId? {
        val saved = store.pointers()
        lastId = maxOf(saved.lastId, stored.maxOfOrNull { it.value } ?: 0)
        previous = saved.previous
        mutableActive.value = saved.active?.takeIf { it in stored } ?: stored.firstOrNull()
        return active.value
    }

    fun nextId(): AccountId = AccountId(++lastId)

    suspend fun activate(id: AccountId) {
        val current = active.value
        if (current != id) {
            previous = current ?: previous
            mutableActive.value = id
        }
        store.savePointers(AccountPointers(active = id, previous = previous, lastId = lastId))
    }
}

package com.xbot.kotogram.core.tdlib.account

import com.xbot.kotogram.core.model.AccountId

/**
 * Which accounts exist. On disk that is the directories next to TDLib's files; on the web those belong
 * to the worker, so the page keeps its own list.
 */
internal interface AccountStorage : AccountPointerStore {
    suspend fun accounts(): List<AccountId>

    suspend fun create(id: AccountId)

    suspend fun delete(id: AccountId)
}

package com.xbot.kotogram.core.tdlib.account

import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.core.tdlib.client.ManagedClientHolder
import com.xbot.kotogram.core.tdlib.store.AccountStore

/** Everything that lives as long as one account does. */
internal interface AccountSession {
    val id: AccountId
    val client: ManagedClientHolder
    val store: AccountStore

    fun close()
}

internal interface AccountSessions {
    /** Opening starts the account's TDLib instance. */
    fun open(id: AccountId): AccountSession

    operator fun get(id: AccountId): AccountSession?
}

package com.xbot.kotogram.core.tdlib.extensions

import com.xbot.kotogram.core.model.Account
import com.xbot.kotogram.core.tdlib.account.AccountSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal val AccountSession.account: Flow<Account>
    get() = combine(
        client.authorizationState,
        store.state.map { it.myId }.distinctUntilChanged()
    ) { state, myId ->
        Account(id = id, authorizationState = state, userId = myId)
    }

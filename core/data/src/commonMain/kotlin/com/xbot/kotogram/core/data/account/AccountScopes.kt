package com.xbot.kotogram.core.data.account

import com.xbot.kotogram.core.data.client.ClientHolder
import com.xbot.kotogram.core.data.environment.directoryName
import com.xbot.kotogram.core.data.store.AccountStore
import com.xbot.kotogram.core.domain.model.Account
import com.xbot.kotogram.core.domain.model.AccountId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.Koin
import org.koin.core.scope.Scope

/** The Koin scope of an open account, for the UI to provide to the account's screens. */
fun Koin.accountScope(id: AccountId): Scope? = getScopeOrNull(id.directoryName)

internal fun Koin.holderOf(id: AccountId): ClientHolder =
    checkNotNull(accountScope(id)) { "Account ${id.value} is not open" }.get()

internal fun Koin.accountOf(id: AccountId): Flow<Account> {
    val accountScope = checkNotNull(accountScope(id)) { "Account ${id.value} is not open" }
    val userId = accountScope.get<AccountStore>().state.map { it.myId }.distinctUntilChanged()
    return combine(accountScope.get<ClientHolder>().authorizationState, userId) { state, myId ->
        Account(id = id, authorizationState = state, userId = myId)
    }
}

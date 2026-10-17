package com.xbot.kotogram.core.domain.repository

import arrow.core.Either
import com.xbot.kotogram.core.domain.model.Account
import com.xbot.kotogram.core.domain.model.AccountId
import kotlinx.coroutines.flow.StateFlow

/**
 * The accounts on this device. TDLib is the source of truth: an account is a TDLib database, and its
 * state is TDLib's. Logging out is a TDLib request on the account; this repository reacts to it.
 */
interface AccountRepository {
    /** In the order they were added. */
    val accounts: StateFlow<List<Account>>

    /** The account the UI shows; `null` only while the accounts are loading. */
    val active: StateFlow<Account?>

    /** Adds an account and makes it active, so its login shows. Switching away before it logs in drops it. */
    suspend fun addAccount(): Either<AccountError, AccountId>

    /** Switches to an authorized account. */
    suspend fun switchTo(id: AccountId): Either<AccountError, Unit>
}

sealed interface AccountError {
    data object UnknownAccount : AccountError

    data object NotAuthorized : AccountError
}

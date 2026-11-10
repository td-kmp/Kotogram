package com.xbot.kotogram.core.account

import arrow.core.Either
import com.xbot.kotogram.core.account.extensions.sendCatching
import io.xbot.tdlib.api.Error
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

interface AppClient {
    suspend fun <R : TdObject> send(function: TdFunction<R>): Either<Error, R>
}

internal class DefaultAppClient(
    private val accounts: AccountManager,
) : AppClient {
    override suspend fun <R : TdObject> send(function: TdFunction<R>): Either<Error, R> =
        session().client.sendCatching(function)

    private suspend fun session(): AccountSession =
        combine(accounts.accounts, accounts.shown) { signedIn, shown -> signedIn.firstOrNull() ?: shown.active }
            .filterNotNull()
            .first()
}

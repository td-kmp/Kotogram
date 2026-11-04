package com.xbot.kotogram.core.tdlib.client

import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject
import kotlinx.coroutines.flow.StateFlow

interface TdSender {
    /** @throws io.xbot.tdlib.client.TdException when TDLib answers with an error */
    suspend fun <R : TdObject> send(function: TdFunction<R>): R
}

/** The TDLib client of one account, as repositories see it. It survives instance restarts. */
interface ClientHolder : TdSender {
    val authorizationState: StateFlow<AuthorizationState?>
}

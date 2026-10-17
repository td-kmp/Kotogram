package com.xbot.kotogram.core.tdlib

import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject
import io.xbot.tdlib.api.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The TDLib client of one account. A TDLib instance can't be reused once closed, but an account
 * outlives its instances (logging out, leaving QR login), so the holder starts a new instance on the
 * account's database after every `authorizationStateClosed`, until the account's scope closes.
 */
interface ClientHolder {
    /** The live instance's state; it continues across instances. */
    val authorizationState: StateFlow<AuthorizationState?>

    /**
     * Every update, across instances: a restart shows up as `authorizationStateClosed` followed by the
     * new instance's updates, so caches reset there.
     */
    val updates: Flow<Update>

    /**
     * Sends to the live instance. TDLib answers requests to an instance that is closing with error 500.
     * @throws io.xbot.tdlib.client.TdException when TDLib answers with an error
     */
    suspend fun <R : TdObject> send(function: TdFunction<R>): R
}

package com.xbot.kotogram.core.tdlib

import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject
import kotlinx.coroutines.flow.StateFlow

/**
 * How far one login's life has got. Sticky and derived in order from TDLib's states, which reach
 * everyone else through a conflated flow that could skip them.
 */
enum class AccountLifecycle { Live, LoggingOut, Ended }

/**
 * The TDLib client of one account. An instance is replaced only inside a login (leaving QR login) or
 * after an unexplained close. A real log out ends the account: no new instance starts.
 */
interface ClientHolder {
    /** The live instance's state; it continues across instances. */
    val authorizationState: StateFlow<AuthorizationState?>

    val lifecycle: StateFlow<AccountLifecycle>

    /**
     * Sends to the live instance. TDLib answers requests to an instance that is closing with error 500,
     * and so does the holder once the account has ended.
     * @throws io.xbot.tdlib.client.TdException when TDLib answers with an error
     */
    suspend fun <R : TdObject> send(function: TdFunction<R>): R

    /** Ends an unauthorized account through TDLib's `destroy`. */
    suspend fun destroy()

    /** Stops for good and waits for TDLib. `false` if TDLib didn't confirm in time. */
    suspend fun shutdown(): Boolean
}

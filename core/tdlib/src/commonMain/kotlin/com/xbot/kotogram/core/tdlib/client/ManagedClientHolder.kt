package com.xbot.kotogram.core.tdlib.client

import kotlinx.coroutines.flow.StateFlow

/**
 * How far one login's life has got. Sticky and derived in order from TDLib's states, which reach
 * everyone else through a conflated flow that could skip them.
 */
internal enum class AccountLifecycle { Live, LoggingOut, Ended }

/**
 * A [ClientHolder] with the controls only the account's owner may use. An instance is replaced only
 * inside a login (leaving QR login) or after an unexplained close: a real log out ends the account.
 */
internal interface ManagedClientHolder :
    ClientHolder,
    AutoCloseable {
    val lifecycle: StateFlow<AccountLifecycle>

    /** Ends an unauthorized account through TDLib's `destroy`. */
    suspend fun destroy()

    /** Stops for good and waits for TDLib. `false` if TDLib didn't confirm in time. */
    suspend fun shutdown(): Boolean
}

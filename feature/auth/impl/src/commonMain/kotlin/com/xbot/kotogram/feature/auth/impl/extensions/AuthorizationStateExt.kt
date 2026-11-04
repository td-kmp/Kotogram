package com.xbot.kotogram.feature.auth.impl.extensions

import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateClosed
import io.xbot.tdlib.api.AuthorizationStateClosing
import io.xbot.tdlib.api.AuthorizationStateLoggingOut
import io.xbot.tdlib.api.AuthorizationStateReady
import io.xbot.tdlib.api.AuthorizationStateWaitTdlibParameters

/**
 * TDLib takes a number in these states; during QR confirmation too, since the repository leaves QR
 * login first. It refuses while starting, closing or once authorized.
 */
internal val AuthorizationState.acceptsPhoneNumber: Boolean
    get() = when (this) {
        AuthorizationStateWaitTdlibParameters,
        AuthorizationStateReady,
        AuthorizationStateLoggingOut,
        AuthorizationStateClosing,
        AuthorizationStateClosed -> false

        else -> true
    }

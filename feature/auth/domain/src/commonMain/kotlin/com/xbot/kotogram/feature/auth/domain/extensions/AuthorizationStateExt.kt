package com.xbot.kotogram.feature.auth.domain.extensions

import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateClosed
import io.xbot.tdlib.api.AuthorizationStateClosing
import io.xbot.tdlib.api.AuthorizationStateLoggingOut
import io.xbot.tdlib.api.AuthorizationStateReady
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.AuthorizationStateWaitTdlibParameters

val AuthorizationState.acceptsPhoneNumber: Boolean
    get() = when (this) {
        AuthorizationStateWaitTdlibParameters,
        is AuthorizationStateWaitOtherDeviceConfirmation,
        AuthorizationStateReady,
        AuthorizationStateLoggingOut,
        AuthorizationStateClosing,
        AuthorizationStateClosed,
        -> false

        else -> true
    }

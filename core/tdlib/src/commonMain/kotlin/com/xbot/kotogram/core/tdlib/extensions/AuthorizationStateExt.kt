package com.xbot.kotogram.core.tdlib.extensions

import com.xbot.kotogram.core.model.extensions.isLoginStep
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateLoggingOut

/** A log out from a login step only leaves QR login; anywhere else it ends the account. */
internal fun AuthorizationState.endsAccountAfter(previous: AuthorizationState?): Boolean =
    this is AuthorizationStateLoggingOut && !previous.isLoginStep

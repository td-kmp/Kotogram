package com.xbot.kotogram.core.tdlib.extensions

import io.xbot.tdlib.api.AuthorizationStateClosed
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateAuthorizationState

internal val Update.closesInstance: Boolean
    get() = this is UpdateAuthorizationState && authorizationState is AuthorizationStateClosed

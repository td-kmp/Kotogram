package com.xbot.kotogram.core.account.extensions

import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateWaitCode
import io.xbot.tdlib.api.AuthorizationStateWaitEmailAddress
import io.xbot.tdlib.api.AuthorizationStateWaitEmailCode
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.AuthorizationStateWaitPassword
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import io.xbot.tdlib.api.AuthorizationStateWaitPremiumPurchase
import io.xbot.tdlib.api.AuthorizationStateWaitRegistration

val AuthorizationState?.isLoginStep: Boolean
    get() = when (this) {
        is AuthorizationStateWaitPhoneNumber,
        is AuthorizationStateWaitPremiumPurchase,
        is AuthorizationStateWaitEmailAddress,
        is AuthorizationStateWaitEmailCode,
        is AuthorizationStateWaitCode,
        is AuthorizationStateWaitOtherDeviceConfirmation,
        is AuthorizationStateWaitRegistration,
        is AuthorizationStateWaitPassword -> true

        else -> false
    }

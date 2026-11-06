package com.xbot.kotogram.feature.auth.impl.extensions

import com.xbot.kotogram.feature.auth.impl.navigation.AuthStep
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateClosed
import io.xbot.tdlib.api.AuthorizationStateClosing
import io.xbot.tdlib.api.AuthorizationStateLoggingOut
import io.xbot.tdlib.api.AuthorizationStateReady
import io.xbot.tdlib.api.AuthorizationStateWaitCode
import io.xbot.tdlib.api.AuthorizationStateWaitEmailAddress
import io.xbot.tdlib.api.AuthorizationStateWaitEmailCode
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.AuthorizationStateWaitPassword
import io.xbot.tdlib.api.AuthorizationStateWaitPremiumPurchase
import io.xbot.tdlib.api.AuthorizationStateWaitRegistration
import io.xbot.tdlib.api.AuthorizationStateWaitTdlibParameters

internal val AuthorizationState.acceptsPhoneNumber: Boolean
    get() = when (this) {
        AuthorizationStateWaitTdlibParameters,
        is AuthorizationStateWaitOtherDeviceConfirmation,
        AuthorizationStateReady,
        AuthorizationStateLoggingOut,
        AuthorizationStateClosing,
        AuthorizationStateClosed -> false

        else -> true
    }

internal fun AuthorizationState.toAuthStep(): AuthStep? = when (this) {
    is AuthorizationStateWaitCode -> AuthStep.Code
    is AuthorizationStateWaitPassword -> AuthStep.Password
    is AuthorizationStateWaitEmailAddress -> AuthStep.EmailAddress
    is AuthorizationStateWaitEmailCode -> AuthStep.EmailCode
    is AuthorizationStateWaitRegistration -> AuthStep.Registration
    is AuthorizationStateWaitPremiumPurchase -> AuthStep.PremiumPurchase
    else -> null
}

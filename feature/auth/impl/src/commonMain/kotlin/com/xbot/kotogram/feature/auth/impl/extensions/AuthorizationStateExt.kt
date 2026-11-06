package com.xbot.kotogram.feature.auth.impl.extensions

import com.xbot.kotogram.feature.auth.api.AuthStep
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateWaitCode
import io.xbot.tdlib.api.AuthorizationStateWaitEmailAddress
import io.xbot.tdlib.api.AuthorizationStateWaitEmailCode
import io.xbot.tdlib.api.AuthorizationStateWaitPassword
import io.xbot.tdlib.api.AuthorizationStateWaitPremiumPurchase
import io.xbot.tdlib.api.AuthorizationStateWaitRegistration

internal fun AuthorizationState.toAuthStep(): AuthStep? = when (this) {
    is AuthorizationStateWaitCode -> AuthStep.Code
    is AuthorizationStateWaitPassword -> AuthStep.Password
    is AuthorizationStateWaitEmailAddress -> AuthStep.EmailAddress
    is AuthorizationStateWaitEmailCode -> AuthStep.EmailCode
    is AuthorizationStateWaitRegistration -> AuthStep.Registration
    is AuthorizationStateWaitPremiumPurchase -> AuthStep.PremiumPurchase
    else -> null
}

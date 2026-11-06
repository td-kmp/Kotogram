package com.xbot.kotogram.feature.auth.impl.navigation

import com.xbot.kotogram.feature.auth.domain.model.LoginMethod

internal fun authBackStack(
    method: LoginMethod,
    step: AuthStep?,
    dismissed: AuthStep?
): List<AuthStep> {
    // TDLib shows a QR code only before the first step, so past it the entry is the phone.
    val entry = when {
        method == LoginMethod.QrCode && step == null -> AuthStep.QrCode
        else -> AuthStep.PhoneNumber
    }
    return listOfNotNull(entry, step?.takeUnless { it == dismissed })
}

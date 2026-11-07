package com.xbot.kotogram.feature.auth.impl.screen.auth

import com.xbot.kotogram.feature.auth.api.AuthStep
import com.xbot.kotogram.feature.auth.domain.model.LoginFlow

internal data class AuthScreenState(
    val flow: LoginFlow,
    val backStack: List<AuthStep>,
    // Never comes back: Telegram for macOS drops "Continue in …" on the first change of step.
    val hasLeftFirstStep: Boolean = false
) {
    val canUseQr: Boolean get() = flow == LoginFlow.QrCodeFirst
}

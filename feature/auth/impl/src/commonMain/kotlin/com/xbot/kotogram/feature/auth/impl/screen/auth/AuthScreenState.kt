package com.xbot.kotogram.feature.auth.impl.screen.auth

import com.xbot.kotogram.feature.auth.api.AuthStep
import com.xbot.kotogram.feature.auth.domain.model.LoginFlow

internal data class AuthScreenState(
    val flow: LoginFlow,
    // Empty until TDLib's state is read, or a restored step flashes the QR code.
    val backStack: List<AuthStep> = emptyList(),
    // Never comes back: Telegram for macOS drops "Continue in …" on the first change of step.
    val hasLeftFirstStep: Boolean = false
) {
    val canUseQr: Boolean get() = flow == LoginFlow.QrCodeFirst
}

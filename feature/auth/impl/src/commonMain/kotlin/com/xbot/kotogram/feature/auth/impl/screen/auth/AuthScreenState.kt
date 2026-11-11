package com.xbot.kotogram.feature.auth.impl.screen.auth

import com.xbot.kotogram.feature.auth.api.AuthStep
import com.xbot.kotogram.feature.auth.domain.model.LoginFlow

internal data class AuthScreenState(
    val flow: LoginFlow,
    val backStack: List<AuthStep> = emptyList(),
    val hasLeftFirstStep: Boolean = false,
) {
    val canUseQr: Boolean get() = flow == LoginFlow.QrCodeFirst
}

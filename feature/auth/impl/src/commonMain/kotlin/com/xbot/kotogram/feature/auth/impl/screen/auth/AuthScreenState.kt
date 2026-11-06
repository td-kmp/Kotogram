package com.xbot.kotogram.feature.auth.impl.screen.auth

import com.xbot.kotogram.feature.auth.domain.model.LoginFlow
import com.xbot.kotogram.feature.auth.impl.navigation.AuthStep

internal data class AuthScreenState(val flow: LoginFlow, val backStack: List<AuthStep>) {
    val canUseQr: Boolean get() = flow == LoginFlow.QrCodeFirst
}

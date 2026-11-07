package com.xbot.kotogram.feature.auth.impl.screen.auth

import com.xbot.kotogram.feature.auth.api.AuthStep
import com.xbot.kotogram.feature.auth.domain.model.LoginFlow
import com.xbot.kotogram.feature.language.domain.model.LanguageSuggestion

internal data class AuthScreenState(
    val flow: LoginFlow,
    val backStack: List<AuthStep>,
    val languageSuggestion: LanguageSuggestion? = null,
    val isSwitchingLanguage: Boolean = false
) {
    val canUseQr: Boolean get() = flow == LoginFlow.QrCodeFirst
}

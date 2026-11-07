package com.xbot.kotogram.feature.auth.impl.screen.auth

internal sealed interface AuthScreenAction {
    data object OnBack : AuthScreenAction

    // TDLib may stay in the same state, so the current step has to be shown again.
    data object OnEntrySubmitted : AuthScreenAction

    data object OnPhoneLoginClick : AuthScreenAction

    data object OnQrLoginClick : AuthScreenAction

    data object OnContinueOnLanguageClick : AuthScreenAction
}

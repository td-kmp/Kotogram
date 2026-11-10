package com.xbot.kotogram.feature.auth.impl.screen.auth

internal sealed interface AuthScreenAction {
    data object OnBack : AuthScreenAction

    data object OnEntrySubmitted : AuthScreenAction

    data object OnPhoneLoginClick : AuthScreenAction

    data object OnQrLoginClick : AuthScreenAction
}

package com.xbot.kotogram.feature.auth.impl.screen.password

internal sealed interface PasswordScreenAction {
    data object OnSubmit : PasswordScreenAction

    data object OnForgotPassword : PasswordScreenAction
}

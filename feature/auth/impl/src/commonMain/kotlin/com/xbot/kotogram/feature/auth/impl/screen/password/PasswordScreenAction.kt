package com.xbot.kotogram.feature.auth.impl.screen.password

internal sealed interface PasswordScreenAction {
    data class OnPasswordChange(val value: String) : PasswordScreenAction

    data class OnRecoveryCodeChange(val value: String) : PasswordScreenAction

    data object OnSubmit : PasswordScreenAction

    data object OnForgotPassword : PasswordScreenAction
}

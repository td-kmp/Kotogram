package com.xbot.kotogram.feature.auth.impl.screen.password

import com.xbot.kotogram.feature.auth.domain.model.AuthError

internal data class PasswordScreenState(
    val hint: String = "",
    val hasRecoveryEmail: Boolean = false,
    val recoveryEmailPattern: String = "",
    val password: String = "",
    /** Recovery code was sent to [recoveryEmailPattern]; the screen asks for it instead of the password. */
    val isRecovering: Boolean = false,
    val recoveryCode: String = "",
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
)

internal sealed interface PasswordScreenAction {
    data class OnPasswordChange(val value: String) : PasswordScreenAction

    data class OnRecoveryCodeChange(val value: String) : PasswordScreenAction

    data object OnSubmit : PasswordScreenAction

    data object OnForgotPassword : PasswordScreenAction
}

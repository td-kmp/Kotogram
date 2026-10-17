package com.xbot.kotogram.feature.auth.presentation.screen.emailcode

import com.xbot.kotogram.feature.auth.domain.model.AuthError

internal data class EmailCodeScreenState(
    val emailPattern: String = "",
    val codeLength: Int = 0,
    val code: String = "",
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
)

internal sealed interface EmailCodeScreenAction {
    data class OnCodeChange(val value: String) : EmailCodeScreenAction

    data object OnSubmit : EmailCodeScreenAction
}

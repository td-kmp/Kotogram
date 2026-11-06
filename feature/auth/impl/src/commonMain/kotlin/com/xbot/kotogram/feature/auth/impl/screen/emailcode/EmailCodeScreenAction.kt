package com.xbot.kotogram.feature.auth.impl.screen.emailcode

internal sealed interface EmailCodeScreenAction {
    data class OnCodeChange(val value: String) : EmailCodeScreenAction

    data object OnSubmit : EmailCodeScreenAction
}

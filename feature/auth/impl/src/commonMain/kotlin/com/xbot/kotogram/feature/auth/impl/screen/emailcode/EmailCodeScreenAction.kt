package com.xbot.kotogram.feature.auth.impl.screen.emailcode

internal sealed interface EmailCodeScreenAction {
    data object OnSubmit : EmailCodeScreenAction
}

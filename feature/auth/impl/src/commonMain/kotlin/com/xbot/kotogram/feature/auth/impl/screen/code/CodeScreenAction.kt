package com.xbot.kotogram.feature.auth.impl.screen.code

internal sealed interface CodeScreenAction {
    data class OnCodeChange(val value: String) : CodeScreenAction

    data object OnSubmit : CodeScreenAction

    data object OnResend : CodeScreenAction
}

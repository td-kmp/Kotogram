package com.xbot.kotogram.feature.auth.impl.screen.code

internal sealed interface CodeScreenAction {
    data object OnSubmit : CodeScreenAction

    data object OnResend : CodeScreenAction
}

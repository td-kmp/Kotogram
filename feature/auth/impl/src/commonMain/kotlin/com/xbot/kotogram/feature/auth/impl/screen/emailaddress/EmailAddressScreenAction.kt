package com.xbot.kotogram.feature.auth.impl.screen.emailaddress

internal sealed interface EmailAddressScreenAction {
    data class OnEmailChange(val value: String) : EmailAddressScreenAction

    data object OnSubmit : EmailAddressScreenAction
}

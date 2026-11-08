package com.xbot.kotogram.feature.auth.impl.screen.emailaddress

internal sealed interface EmailAddressScreenAction {
    data object OnSubmit : EmailAddressScreenAction
}

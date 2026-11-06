package com.xbot.kotogram.feature.auth.impl.screen.registration

internal sealed interface RegistrationScreenAction {
    data class OnFirstNameChange(val value: String) : RegistrationScreenAction

    data class OnLastNameChange(val value: String) : RegistrationScreenAction

    data class OnTermsVisibilityChange(val visible: Boolean) : RegistrationScreenAction

    data object OnSubmit : RegistrationScreenAction
}

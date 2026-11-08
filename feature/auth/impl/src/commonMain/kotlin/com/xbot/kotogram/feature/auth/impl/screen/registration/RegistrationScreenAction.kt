package com.xbot.kotogram.feature.auth.impl.screen.registration

internal sealed interface RegistrationScreenAction {
    data class OnTermsVisibilityChange(val visible: Boolean) : RegistrationScreenAction

    class OnPhotoPicked(val jpeg: ByteArray) : RegistrationScreenAction

    data object OnSubmit : RegistrationScreenAction
}

package com.xbot.kotogram.feature.auth.presentation.screen.registration

import com.xbot.kotogram.feature.auth.domain.model.AuthError

internal data class RegistrationScreenState(
    val firstName: String = "",
    val lastName: String = "",
    val termsOfService: String = "",
    val isTermsVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
) {
    val canSubmit: Boolean get() = firstName.isNotBlank() && !isSubmitting
}

internal sealed interface RegistrationScreenAction {
    data class OnFirstNameChange(val value: String) : RegistrationScreenAction

    data class OnLastNameChange(val value: String) : RegistrationScreenAction

    data class OnTermsVisibilityChange(val visible: Boolean) : RegistrationScreenAction

    data object OnSubmit : RegistrationScreenAction
}

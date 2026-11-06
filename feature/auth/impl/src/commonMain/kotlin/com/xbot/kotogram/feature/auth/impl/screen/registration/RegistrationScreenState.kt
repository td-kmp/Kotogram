package com.xbot.kotogram.feature.auth.impl.screen.registration

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

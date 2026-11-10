package com.xbot.kotogram.feature.auth.impl.screen.registration

import com.xbot.kotogram.feature.auth.domain.model.AuthError

internal data class RegistrationScreenState(
    val termsOfService: String = "",
    val isTermsVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: AuthError? = null,
)

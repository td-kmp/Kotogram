package com.xbot.kotogram.feature.auth.impl.screen.emailaddress

import com.xbot.kotogram.feature.auth.domain.model.AuthError

internal data class EmailAddressScreenState(
    val email: String = "",
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
)

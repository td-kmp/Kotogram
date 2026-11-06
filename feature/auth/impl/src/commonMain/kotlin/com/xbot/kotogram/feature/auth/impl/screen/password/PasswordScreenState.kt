package com.xbot.kotogram.feature.auth.impl.screen.password

import com.xbot.kotogram.feature.auth.domain.model.AuthError

internal data class PasswordScreenState(
    val hint: String = "",
    val hasRecoveryEmail: Boolean = false,
    val recoveryEmailPattern: String = "",
    val password: String = "",
    val isRecovering: Boolean = false,
    val recoveryCode: String = "",
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
)

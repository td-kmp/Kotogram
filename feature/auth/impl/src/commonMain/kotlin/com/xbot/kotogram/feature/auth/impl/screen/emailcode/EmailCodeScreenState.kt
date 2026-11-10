package com.xbot.kotogram.feature.auth.impl.screen.emailcode

import com.xbot.kotogram.feature.auth.domain.model.AuthError

internal data class EmailCodeScreenState(
    val emailPattern: String = "",
    val codeLength: Int = 0,
    val isSubmitting: Boolean = false,
    val error: AuthError? = null,
)

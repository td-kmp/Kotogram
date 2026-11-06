package com.xbot.kotogram.feature.auth.impl.screen.code

import com.xbot.kotogram.feature.auth.domain.model.AuthError
import io.xbot.tdlib.api.AuthenticationCodeInfo

internal data class CodeScreenState(
    val info: AuthenticationCodeInfo? = null,
    val code: String = "",
    val resendSecondsLeft: Int = 0,
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
) {
    val canResend: Boolean get() = info?.nextType != null && resendSecondsLeft == 0
}

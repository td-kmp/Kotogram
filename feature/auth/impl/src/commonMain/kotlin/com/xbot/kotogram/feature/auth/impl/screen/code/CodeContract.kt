package com.xbot.kotogram.feature.auth.impl.screen.code

import com.xbot.kotogram.feature.auth.domain.model.AuthError
import io.xbot.tdlib.api.AuthenticationCodeInfo

internal data class CodeScreenState(
    val info: AuthenticationCodeInfo? = null,
    val code: String = "",
    /** Seconds until [AuthenticationCodeInfo.nextType] can be requested; 0 when resending is allowed. */
    val resendSecondsLeft: Int = 0,
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
) {
    val canResend: Boolean get() = info?.nextType != null && resendSecondsLeft == 0
}

internal sealed interface CodeScreenAction {
    data class OnCodeChange(val value: String) : CodeScreenAction

    data object OnSubmit : CodeScreenAction

    data object OnResend : CodeScreenAction
}

package com.xbot.kotogram.feature.auth.presentation.screen.code

import com.xbot.kotogram.core.domain.model.AuthenticationCodeInfo
import com.xbot.kotogram.core.domain.model.AuthenticationCodeType
import com.xbot.kotogram.feature.auth.domain.model.AuthError

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

/** The code's length when TDLib knows it; words, phrases and flash calls have none. */
internal val AuthenticationCodeType.length: Int?
    get() = when (this) {
        is AuthenticationCodeType.TelegramMessage -> length

        is AuthenticationCodeType.Sms -> length

        is AuthenticationCodeType.Call -> length

        is AuthenticationCodeType.MissedCall -> length

        is AuthenticationCodeType.Fragment -> length

        is AuthenticationCodeType.FirebaseAndroid -> length

        is AuthenticationCodeType.FirebaseIos -> length

        is AuthenticationCodeType.SmsWord,
        is AuthenticationCodeType.SmsPhrase,
        is AuthenticationCodeType.FlashCall -> null
    }

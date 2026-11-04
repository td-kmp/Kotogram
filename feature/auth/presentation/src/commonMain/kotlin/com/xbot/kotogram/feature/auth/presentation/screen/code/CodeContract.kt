package com.xbot.kotogram.feature.auth.presentation.screen.code

import com.xbot.kotogram.feature.auth.domain.model.AuthError
import io.xbot.tdlib.api.AuthenticationCodeInfo
import io.xbot.tdlib.api.AuthenticationCodeType
import io.xbot.tdlib.api.AuthenticationCodeTypeCall
import io.xbot.tdlib.api.AuthenticationCodeTypeFirebaseAndroid
import io.xbot.tdlib.api.AuthenticationCodeTypeFirebaseIos
import io.xbot.tdlib.api.AuthenticationCodeTypeFlashCall
import io.xbot.tdlib.api.AuthenticationCodeTypeFragment
import io.xbot.tdlib.api.AuthenticationCodeTypeMissedCall
import io.xbot.tdlib.api.AuthenticationCodeTypeSms
import io.xbot.tdlib.api.AuthenticationCodeTypeSmsPhrase
import io.xbot.tdlib.api.AuthenticationCodeTypeSmsWord
import io.xbot.tdlib.api.AuthenticationCodeTypeTelegramMessage

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
        is AuthenticationCodeTypeTelegramMessage -> length

        is AuthenticationCodeTypeSms -> length

        is AuthenticationCodeTypeCall -> length

        is AuthenticationCodeTypeMissedCall -> length

        is AuthenticationCodeTypeFragment -> length

        is AuthenticationCodeTypeFirebaseAndroid -> length

        is AuthenticationCodeTypeFirebaseIos -> length

        is AuthenticationCodeTypeSmsWord,
        is AuthenticationCodeTypeSmsPhrase,
        is AuthenticationCodeTypeFlashCall -> null
    }

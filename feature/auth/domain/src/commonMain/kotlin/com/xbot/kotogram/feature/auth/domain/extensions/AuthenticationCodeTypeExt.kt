package com.xbot.kotogram.feature.auth.domain.extensions

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

val AuthenticationCodeType.length: Int?
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

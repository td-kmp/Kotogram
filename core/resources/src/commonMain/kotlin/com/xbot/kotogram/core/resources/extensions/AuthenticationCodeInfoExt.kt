package com.xbot.kotogram.core.resources.extensions

import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.StringResource
import com.xbot.kotogram.core.resources.code_sent_call
import com.xbot.kotogram.core.resources.code_sent_flash_call
import com.xbot.kotogram.core.resources.code_sent_fragment
import com.xbot.kotogram.core.resources.code_sent_missed_call
import com.xbot.kotogram.core.resources.code_sent_sms
import com.xbot.kotogram.core.resources.code_sent_sms_phrase
import com.xbot.kotogram.core.resources.code_sent_sms_word
import com.xbot.kotogram.core.resources.code_sent_telegram
import io.xbot.tdlib.api.AuthenticationCodeInfo
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

val AuthenticationCodeInfo.description: StringResource
    get() = when (val type = type) {
        is AuthenticationCodeTypeTelegramMessage ->
            StringResource.Text(Res.string.code_sent_telegram)

        is AuthenticationCodeTypeSms,
        is AuthenticationCodeTypeFirebaseAndroid,
        is AuthenticationCodeTypeFirebaseIos ->
            StringResource.Text(Res.string.code_sent_sms, phoneNumber)

        is AuthenticationCodeTypeSmsWord ->
            StringResource.Text(Res.string.code_sent_sms_word, phoneNumber, type.firstLetter)

        is AuthenticationCodeTypeSmsPhrase ->
            StringResource.Text(Res.string.code_sent_sms_phrase, phoneNumber, type.firstWord)

        is AuthenticationCodeTypeCall -> StringResource.Text(Res.string.code_sent_call, phoneNumber)

        is AuthenticationCodeTypeFlashCall ->
            StringResource.Text(Res.string.code_sent_flash_call, phoneNumber)

        is AuthenticationCodeTypeMissedCall ->
            StringResource.Text(
                Res.string.code_sent_missed_call,
                type.phoneNumberPrefix,
                type.length
            )

        is AuthenticationCodeTypeFragment -> StringResource.Text(Res.string.code_sent_fragment)
    }

package com.xbot.kotogram.core.resources.extensions

import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.StringResource
import com.xbot.kotogram.core.resources.login_enterphrasebeginningtext
import com.xbot.kotogram.core.resources.login_enterwordbeginningtext
import com.xbot.kotogram.core.resources.login_new_code_callinfo
import com.xbot.kotogram.core.resources.login_new_code_codeinfo
import com.xbot.kotogram.core.resources.login_new_code_fragmentinfo
import com.xbot.kotogram.core.resources.login_new_code_smsinfo
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
            StringResource.Text(Res.string.login_new_code_codeinfo, phoneNumber)

        is AuthenticationCodeTypeSms,
        is AuthenticationCodeTypeFirebaseAndroid,
        is AuthenticationCodeTypeFirebaseIos ->
            StringResource.Text(Res.string.login_new_code_smsinfo, phoneNumber)

        is AuthenticationCodeTypeSmsWord ->
            StringResource.Text(Res.string.login_enterwordbeginningtext, type.firstLetter, phoneNumber)

        is AuthenticationCodeTypeSmsPhrase ->
            StringResource.Text(Res.string.login_enterphrasebeginningtext, type.firstWord, phoneNumber)

        is AuthenticationCodeTypeCall,
        is AuthenticationCodeTypeFlashCall,
        is AuthenticationCodeTypeMissedCall ->
            StringResource.Text(Res.string.login_new_code_callinfo, phoneNumber)

        is AuthenticationCodeTypeFragment ->
            StringResource.Text(Res.string.login_new_code_fragmentinfo, phoneNumber)
    }

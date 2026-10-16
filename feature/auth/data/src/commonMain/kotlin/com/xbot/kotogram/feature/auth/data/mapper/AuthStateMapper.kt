package com.xbot.kotogram.feature.auth.data.mapper

import com.xbot.kotogram.feature.auth.domain.model.AuthState
import com.xbot.kotogram.feature.auth.domain.model.CodeInfo
import com.xbot.kotogram.feature.auth.domain.model.CodeType
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
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateClosed
import io.xbot.tdlib.api.AuthorizationStateClosing
import io.xbot.tdlib.api.AuthorizationStateLoggingOut
import io.xbot.tdlib.api.AuthorizationStateReady
import io.xbot.tdlib.api.AuthorizationStateWaitCode
import io.xbot.tdlib.api.AuthorizationStateWaitEmailAddress
import io.xbot.tdlib.api.AuthorizationStateWaitEmailCode
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.AuthorizationStateWaitPassword
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import io.xbot.tdlib.api.AuthorizationStateWaitPremiumPurchase
import io.xbot.tdlib.api.AuthorizationStateWaitRegistration
import io.xbot.tdlib.api.AuthorizationStateWaitTdlibParameters

internal fun AuthorizationState.toDomain(): AuthState = when (this) {
    AuthorizationStateWaitTdlibParameters -> AuthState.Initializing

    AuthorizationStateWaitPhoneNumber -> AuthState.WaitPhoneNumber

    is AuthorizationStateWaitOtherDeviceConfirmation -> AuthState.WaitQrConfirmation(link)

    is AuthorizationStateWaitCode -> AuthState.WaitCode(codeInfo.toDomain())

    is AuthorizationStateWaitEmailAddress -> AuthState.WaitEmailAddress(allowAppleId, allowGoogleId)

    is AuthorizationStateWaitEmailCode -> AuthState.WaitEmailCode(
        emailPattern = codeInfo.emailAddressPattern,
        codeLength = codeInfo.length,
        allowAppleId = allowAppleId,
        allowGoogleId = allowGoogleId
    )

    is AuthorizationStateWaitPassword -> AuthState.WaitPassword(
        hint = passwordHint,
        hasRecoveryEmail = hasRecoveryEmailAddress,
        recoveryEmailPattern = recoveryEmailAddressPattern
    )

    is AuthorizationStateWaitRegistration -> AuthState.WaitRegistration(
        termsOfService = termsOfService.text.text,
        minUserAge = termsOfService.minUserAge
    )

    is AuthorizationStateWaitPremiumPurchase -> AuthState.Unsupported(
        "Telegram Premium purchase is required to log in"
    )

    AuthorizationStateReady -> AuthState.Ready

    AuthorizationStateLoggingOut -> AuthState.LoggingOut

    // TelegramClient starts a new instance right after these, which reports its own state.
    AuthorizationStateClosing, AuthorizationStateClosed -> AuthState.Initializing
}

private fun AuthenticationCodeInfo.toDomain() = CodeInfo(
    phoneNumber = phoneNumber,
    type = type.toDomain(),
    nextType = nextType?.toDomain(),
    timeoutSeconds = timeout
)

private fun AuthenticationCodeType.toDomain(): CodeType = when (this) {
    is AuthenticationCodeTypeTelegramMessage -> CodeType.TelegramMessage(length)
    is AuthenticationCodeTypeSms -> CodeType.Sms(length)
    is AuthenticationCodeTypeSmsWord -> CodeType.SmsWord(firstLetter)
    is AuthenticationCodeTypeSmsPhrase -> CodeType.SmsPhrase(firstWord)
    is AuthenticationCodeTypeCall -> CodeType.Call(length)
    is AuthenticationCodeTypeFlashCall -> CodeType.FlashCall(pattern)
    is AuthenticationCodeTypeMissedCall -> CodeType.MissedCall(phoneNumberPrefix, length)
    is AuthenticationCodeTypeFragment -> CodeType.Fragment(url, length)
    is AuthenticationCodeTypeFirebaseAndroid -> CodeType.Firebase(length)
    is AuthenticationCodeTypeFirebaseIos -> CodeType.Firebase(length)
}

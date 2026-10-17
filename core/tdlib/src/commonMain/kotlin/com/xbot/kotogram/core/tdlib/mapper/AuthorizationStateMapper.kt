package com.xbot.kotogram.core.tdlib.mapper

import com.xbot.kotogram.core.domain.model.AuthenticationCodeInfo
import com.xbot.kotogram.core.domain.model.AuthenticationCodeType
import com.xbot.kotogram.core.domain.model.AuthorizationState
import com.xbot.kotogram.core.domain.model.EmailAddressAuthenticationCodeInfo
import com.xbot.kotogram.core.domain.model.EmailAddressResetState
import com.xbot.kotogram.core.domain.model.FirebaseDeviceVerificationParameters
import com.xbot.kotogram.core.domain.model.TermsOfService
import io.xbot.tdlib.api.AuthenticationCodeInfo as TdAuthenticationCodeInfo
import io.xbot.tdlib.api.AuthenticationCodeType as TdAuthenticationCodeType
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
import io.xbot.tdlib.api.AuthorizationState as TdAuthorizationState
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
import io.xbot.tdlib.api.EmailAddressAuthenticationCodeInfo as TdEmailAddressAuthenticationCodeInfo
import io.xbot.tdlib.api.EmailAddressResetState as TdEmailAddressResetState
import io.xbot.tdlib.api.EmailAddressResetStateAvailable
import io.xbot.tdlib.api.EmailAddressResetStatePending
import io.xbot.tdlib.api.FirebaseDeviceVerificationParameters as TdFirebaseDeviceVerificationParameters
import io.xbot.tdlib.api.FirebaseDeviceVerificationParametersPlayIntegrity
import io.xbot.tdlib.api.FirebaseDeviceVerificationParametersSafetyNet
import io.xbot.tdlib.api.TermsOfService as TdTermsOfService
import kotlin.time.Duration.Companion.seconds

fun TdAuthorizationState.toDomain(): AuthorizationState = when (this) {
    AuthorizationStateWaitTdlibParameters -> AuthorizationState.WaitTdlibParameters

    AuthorizationStateWaitPhoneNumber -> AuthorizationState.WaitPhoneNumber

    is AuthorizationStateWaitPremiumPurchase -> AuthorizationState.WaitPremiumPurchase(
        storeProductId = storeProductId,
        premiumDayCount = premiumDayCount,
        supportEmailAddress = supportEmailAddress,
        supportEmailSubject = supportEmailSubject
    )

    is AuthorizationStateWaitEmailAddress ->
        AuthorizationState.WaitEmailAddress(allowAppleId, allowGoogleId)

    is AuthorizationStateWaitEmailCode -> AuthorizationState.WaitEmailCode(
        allowAppleId = allowAppleId,
        allowGoogleId = allowGoogleId,
        codeInfo = codeInfo.required().toDomain(),
        emailAddressResetState = emailAddressResetState?.toDomain()
    )

    is AuthorizationStateWaitCode -> AuthorizationState.WaitCode(codeInfo.required().toDomain())

    is AuthorizationStateWaitOtherDeviceConfirmation ->
        AuthorizationState.WaitOtherDeviceConfirmation(link)

    is AuthorizationStateWaitRegistration ->
        AuthorizationState.WaitRegistration(termsOfService.required().toDomain())

    is AuthorizationStateWaitPassword -> AuthorizationState.WaitPassword(
        passwordHint = passwordHint,
        hasRecoveryEmailAddress = hasRecoveryEmailAddress,
        hasPassportData = hasPassportData,
        recoveryEmailAddressPattern = recoveryEmailAddressPattern
    )

    AuthorizationStateReady -> AuthorizationState.Ready

    AuthorizationStateLoggingOut -> AuthorizationState.LoggingOut

    AuthorizationStateClosing -> AuthorizationState.Closing

    AuthorizationStateClosed -> AuthorizationState.Closed
}

private fun TdAuthenticationCodeInfo.toDomain() = AuthenticationCodeInfo(
    phoneNumber = phoneNumber,
    type = type.required().toDomain(),
    nextType = nextType?.toDomain(),
    timeout = timeout.seconds
)

private fun TdAuthenticationCodeType.toDomain(): AuthenticationCodeType = when (this) {
    is AuthenticationCodeTypeTelegramMessage -> AuthenticationCodeType.TelegramMessage(length)

    is AuthenticationCodeTypeSms -> AuthenticationCodeType.Sms(length)

    is AuthenticationCodeTypeSmsWord -> AuthenticationCodeType.SmsWord(firstLetter)

    is AuthenticationCodeTypeSmsPhrase -> AuthenticationCodeType.SmsPhrase(firstWord)

    is AuthenticationCodeTypeCall -> AuthenticationCodeType.Call(length)

    is AuthenticationCodeTypeFlashCall -> AuthenticationCodeType.FlashCall(pattern)

    is AuthenticationCodeTypeMissedCall ->
        AuthenticationCodeType.MissedCall(phoneNumberPrefix, length)

    is AuthenticationCodeTypeFragment -> AuthenticationCodeType.Fragment(url, length)

    is AuthenticationCodeTypeFirebaseAndroid ->
        AuthenticationCodeType.FirebaseAndroid(
            deviceVerificationParameters = deviceVerificationParameters.required().toDomain(),
            length = length
        )

    is AuthenticationCodeTypeFirebaseIos ->
        AuthenticationCodeType.FirebaseIos(receipt, pushTimeout.seconds, length)
}

private fun TdFirebaseDeviceVerificationParameters.toDomain() = when (this) {
    is FirebaseDeviceVerificationParametersSafetyNet ->
        FirebaseDeviceVerificationParameters.SafetyNet(nonce)

    is FirebaseDeviceVerificationParametersPlayIntegrity ->
        FirebaseDeviceVerificationParameters.PlayIntegrity(nonce, cloudProjectNumber)
}

private fun TdEmailAddressAuthenticationCodeInfo.toDomain() =
    EmailAddressAuthenticationCodeInfo(emailAddressPattern = emailAddressPattern, length = length)

private fun TdEmailAddressResetState.toDomain(): EmailAddressResetState = when (this) {
    is EmailAddressResetStateAvailable -> EmailAddressResetState.Available(waitPeriod.seconds)
    is EmailAddressResetStatePending -> EmailAddressResetState.Pending(resetIn.seconds)
}

private fun TdTermsOfService.toDomain() = TermsOfService(
    text = text.required().toDomain(),
    minUserAge = minUserAge,
    showPopup = showPopup
)

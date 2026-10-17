package com.xbot.kotogram.core.domain.model

import kotlin.time.Duration

/** TDLib's `AuthorizationState`, field for field. */
sealed interface AuthorizationState {
    data object WaitTdlibParameters : AuthorizationState

    data object WaitPhoneNumber : AuthorizationState

    data class WaitPremiumPurchase(
        val storeProductId: String,
        val premiumDayCount: Int,
        val supportEmailAddress: String,
        val supportEmailSubject: String
    ) : AuthorizationState

    data class WaitEmailAddress(val allowAppleId: Boolean, val allowGoogleId: Boolean) :
        AuthorizationState

    data class WaitEmailCode(
        val allowAppleId: Boolean,
        val allowGoogleId: Boolean,
        val codeInfo: EmailAddressAuthenticationCodeInfo,
        val emailAddressResetState: EmailAddressResetState?
    ) : AuthorizationState

    data class WaitCode(val codeInfo: AuthenticationCodeInfo) : AuthorizationState

    data class WaitOtherDeviceConfirmation(val link: String) : AuthorizationState

    data class WaitRegistration(val termsOfService: TermsOfService) : AuthorizationState

    data class WaitPassword(
        val passwordHint: String,
        val hasRecoveryEmailAddress: Boolean,
        val hasPassportData: Boolean,
        val recoveryEmailAddressPattern: String
    ) : AuthorizationState

    data object Ready : AuthorizationState

    data object LoggingOut : AuthorizationState

    data object Closing : AuthorizationState

    data object Closed : AuthorizationState
}

data class AuthenticationCodeInfo(
    val phoneNumber: String,
    val type: AuthenticationCodeType,
    val nextType: AuthenticationCodeType?,
    /** Until [nextType] can be requested; zero when it can be requested now. */
    val timeout: Duration
)

sealed interface AuthenticationCodeType {
    data class TelegramMessage(val length: Int) : AuthenticationCodeType

    data class Sms(val length: Int) : AuthenticationCodeType

    data class SmsWord(val firstLetter: String) : AuthenticationCodeType

    data class SmsPhrase(val firstWord: String) : AuthenticationCodeType

    data class Call(val length: Int) : AuthenticationCodeType

    data class FlashCall(val pattern: String) : AuthenticationCodeType

    data class MissedCall(val phoneNumberPrefix: String, val length: Int) : AuthenticationCodeType

    data class Fragment(val url: String, val length: Int) : AuthenticationCodeType

    data class FirebaseAndroid(
        val deviceVerificationParameters: FirebaseDeviceVerificationParameters,
        val length: Int
    ) : AuthenticationCodeType

    data class FirebaseIos(val receipt: String, val pushTimeout: Duration, val length: Int) :
        AuthenticationCodeType
}

sealed interface FirebaseDeviceVerificationParameters {
    class SafetyNet(val nonce: ByteArray) : FirebaseDeviceVerificationParameters

    data class PlayIntegrity(val nonce: String, val cloudProjectNumber: Long) :
        FirebaseDeviceVerificationParameters
}

data class EmailAddressAuthenticationCodeInfo(val emailAddressPattern: String, val length: Int)

sealed interface EmailAddressResetState {
    data class Available(val waitPeriod: Duration) : EmailAddressResetState

    data class Pending(val resetIn: Duration) : EmailAddressResetState
}

data class TermsOfService(val text: FormattedText, val minUserAge: Int, val showPopup: Boolean)

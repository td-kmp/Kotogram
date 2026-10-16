package com.xbot.kotogram.feature.auth.domain.model

/** Where the login flow is, mirrored from TDLib's authorization state. */
sealed interface AuthState {
    /** TDLib is starting up or applying its parameters. */
    data object Initializing : AuthState

    data object WaitPhoneNumber : AuthState

    /** Waiting for another logged-in device to scan [link] as a QR code. The link rotates. */
    data class WaitQrConfirmation(val link: String) : AuthState

    data class WaitCode(val info: CodeInfo) : AuthState

    data class WaitEmailAddress(val allowAppleId: Boolean, val allowGoogleId: Boolean) : AuthState

    data class WaitEmailCode(
        val emailPattern: String,
        val codeLength: Int,
        val allowAppleId: Boolean,
        val allowGoogleId: Boolean
    ) : AuthState

    data class WaitPassword(
        val hint: String,
        val hasRecoveryEmail: Boolean,
        val recoveryEmailPattern: String
    ) : AuthState

    data class WaitRegistration(val termsOfService: String, val minUserAge: Int) : AuthState

    /** Login needs something this client can't do (e.g. an in-store Premium purchase). */
    data class Unsupported(val reason: String) : AuthState

    data object Ready : AuthState

    data object LoggingOut : AuthState
}

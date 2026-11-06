package com.xbot.kotogram.feature.auth.data.extensions

import com.xbot.kotogram.core.account.extensions.isFlood
import com.xbot.kotogram.core.account.extensions.retryAfter
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import io.xbot.tdlib.api.Error

private val invalidCodeErrors = setOf("PHONE_CODE_INVALID", "PHONE_CODE_EMPTY")

internal fun Error.toAuthError(): AuthError = when {
    isFlood -> AuthError.TooManyAttempts(retryAfter)
    message == "PHONE_NUMBER_INVALID" -> AuthError.InvalidPhoneNumber
    message == "PHONE_NUMBER_BANNED" -> AuthError.PhoneNumberBanned
    message in invalidCodeErrors -> AuthError.InvalidCode
    message == "PHONE_CODE_EXPIRED" -> AuthError.CodeExpired
    message == "PASSWORD_HASH_INVALID" -> AuthError.InvalidPassword
    message.startsWith("EMAIL_") -> AuthError.InvalidEmail
    message.startsWith("FIRSTNAME_") || message.startsWith("LASTNAME_") -> AuthError.InvalidName
    else -> AuthError.Unknown(message)
}

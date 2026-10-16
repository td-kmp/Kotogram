package com.xbot.kotogram.feature.auth.data.mapper

import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.AuthException
import io.xbot.tdlib.client.TdException
import kotlinx.coroutines.CancellationException

private const val TOO_MANY_REQUESTS = 429
private val retryAfter = Regex("""retry after (\d+)""")
private val invalidCodeErrors = setOf("PHONE_CODE_INVALID", "PHONE_CODE_EMPTY")

/** Runs a TDLib call, turning [TdException] into [AuthException]. */
internal suspend inline fun <T> tdCall(block: suspend () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: TdException) {
    throw AuthException(e.toAuthError(), e)
}

internal fun TdException.toAuthError(): AuthError {
    val message = error.message
    if (isFlood(code, message)) return AuthError.TooManyAttempts(retryAfterSeconds(message))
    return when {
        message == "PHONE_NUMBER_INVALID" -> AuthError.InvalidPhoneNumber
        message == "PHONE_NUMBER_BANNED" -> AuthError.PhoneNumberBanned
        message in invalidCodeErrors -> AuthError.InvalidCode
        message == "PHONE_CODE_EXPIRED" -> AuthError.CodeExpired
        message == "PASSWORD_HASH_INVALID" -> AuthError.InvalidPassword
        message.startsWith("EMAIL_") -> AuthError.InvalidEmail
        message.startsWith("FIRSTNAME_") || message.startsWith("LASTNAME_") -> AuthError.InvalidName
        else -> AuthError.Unknown(message)
    }
}

private fun isFlood(code: Int, message: String): Boolean =
    code == TOO_MANY_REQUESTS || message.startsWith("FLOOD_WAIT") || message == "PHONE_NUMBER_FLOOD"

// TDLib reports "Too Many Requests: retry after N"; the server's raw form is FLOOD_WAIT_N.
private fun retryAfterSeconds(message: String): Int? =
    retryAfter.find(message)?.groupValues?.get(1)?.toIntOrNull()
        ?: message.substringAfterLast('_').toIntOrNull()

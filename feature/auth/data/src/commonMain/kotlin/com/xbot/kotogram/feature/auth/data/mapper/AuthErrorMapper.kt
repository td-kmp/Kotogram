package com.xbot.kotogram.feature.auth.data.mapper

import arrow.core.raise.Raise
import arrow.core.raise.catch
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import io.xbot.tdlib.client.TdException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.SerializationException

private const val TOO_MANY_REQUESTS = 429
private val retryAfter = Regex("""retry after (\d+)""")
private val invalidCodeErrors = setOf("PHONE_CODE_INVALID", "PHONE_CODE_EMPTY")

/** Runs a TDLib request, raising TDLib's error as an [AuthError]. */
internal inline fun <T> Raise<AuthError>.request(block: () -> T): T = catch({
    catch(block) { e: TdException -> raise(e.toAuthError()) }
}) { e: SerializationException ->
    // The library surfaces an answer it can't decode this way.
    raise(AuthError.Unknown(e.message.orEmpty()))
}

internal fun TdException.toAuthError(): AuthError {
    val message = error.message
    if (isFlood(code, message)) return AuthError.TooManyAttempts(retryAfter(message))
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
private fun retryAfter(message: String): Duration? = (
    retryAfter.find(message)?.groupValues?.get(1)?.toIntOrNull()
        ?: message.substringAfterLast('_').toIntOrNull()
    )
    ?.seconds

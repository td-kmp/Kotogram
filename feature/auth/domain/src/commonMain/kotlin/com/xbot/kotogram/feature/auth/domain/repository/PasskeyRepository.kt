package com.xbot.kotogram.feature.auth.domain.repository

import arrow.core.Either
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.PasskeyCredential

/** Telegram's side of passkey login; the device side is [PasskeyAuthenticator]. */
interface PasskeyRepository {
    /** WebAuthn request options as JSON. */
    suspend fun getAuthenticationPasskeyParameters(): Either<AuthError, String>

    suspend fun checkAuthenticationPasskey(credential: PasskeyCredential): Either<AuthError, Unit>
}

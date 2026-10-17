package com.xbot.kotogram.feature.auth.domain.repository

import arrow.core.Either
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.PasskeyCredential

/** The platform's WebAuthn authenticator (Credential Manager, ASAuthorization, ...). */
interface PasskeyAuthenticator {
    suspend fun isAvailable(): Boolean

    suspend fun authenticate(requestJson: String): Either<AuthError, PasskeyCredential>
}

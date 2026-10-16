package com.xbot.kotogram.feature.auth.domain.repository

import com.xbot.kotogram.feature.auth.domain.model.PasskeyCredential

/** The platform's WebAuthn authenticator (Credential Manager, ASAuthorization, ...). */
interface PasskeyAuthenticator {
    suspend fun isAvailable(): Boolean

    /** @throws com.xbot.kotogram.feature.auth.domain.model.AuthException when cancelled or unavailable. */
    suspend fun authenticate(requestJson: String): PasskeyCredential
}

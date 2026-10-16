package com.xbot.kotogram.feature.auth.domain.repository

import com.xbot.kotogram.feature.auth.domain.model.PasskeyCredential

/** Telegram's side of passkey login; the device side is [PasskeyAuthenticator]. */
interface PasskeyRepository {
    /** WebAuthn request options as JSON. */
    suspend fun requestJson(): String

    suspend fun checkCredential(credential: PasskeyCredential)
}

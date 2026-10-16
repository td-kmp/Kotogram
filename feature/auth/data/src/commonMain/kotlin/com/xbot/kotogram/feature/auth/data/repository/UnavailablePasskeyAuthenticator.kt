package com.xbot.kotogram.feature.auth.data.repository

import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.AuthException
import com.xbot.kotogram.feature.auth.domain.model.PasskeyCredential
import com.xbot.kotogram.feature.auth.domain.repository.PasskeyAuthenticator

/**
 * Telegram's passkeys are bound to telegram.org, and its assetlinks.json / apple-app-site-association
 * only trust the official apps, so platform authenticators refuse other clients. Kept behind the
 * interface so a real implementation can drop in if that changes.
 */
internal class UnavailablePasskeyAuthenticator : PasskeyAuthenticator {
    override suspend fun isAvailable(): Boolean = false

    override suspend fun authenticate(requestJson: String): PasskeyCredential =
        throw AuthException(AuthError.PasskeyUnavailable)
}

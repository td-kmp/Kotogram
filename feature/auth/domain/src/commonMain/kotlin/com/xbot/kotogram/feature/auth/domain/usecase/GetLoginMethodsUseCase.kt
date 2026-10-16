package com.xbot.kotogram.feature.auth.domain.usecase

import com.xbot.kotogram.feature.auth.domain.model.LoginMethod
import com.xbot.kotogram.feature.auth.domain.repository.PasskeyAuthenticator

/** QR login exists only where the user can scan with another device (desktop), as in the official apps. */
class GetLoginMethodsUseCase(
    private val supportsQrLogin: Boolean,
    private val passkeyAuthenticator: PasskeyAuthenticator
) {
    suspend operator fun invoke(): Set<LoginMethod> = buildSet {
        if (supportsQrLogin) add(LoginMethod.QrCode)
        add(LoginMethod.PhoneNumber)
        if (passkeyAuthenticator.isAvailable()) add(LoginMethod.Passkey)
    }
}

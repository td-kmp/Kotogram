package com.xbot.kotogram.feature.auth.domain.usecase

import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.feature.auth.domain.model.LoginMethod
import com.xbot.kotogram.feature.auth.domain.repository.LoginCapabilities
import com.xbot.kotogram.feature.auth.domain.repository.PasskeyAuthenticator
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Scope

@Scope(AccountId::class)
@Factory
class GetLoginMethodsUseCase(
    private val loginCapabilities: LoginCapabilities,
    private val passkeyAuthenticator: PasskeyAuthenticator
) {
    suspend operator fun invoke(): Set<LoginMethod> = buildSet {
        if (loginCapabilities.supportsQrLogin) add(LoginMethod.QrCode)
        add(LoginMethod.PhoneNumber)
        if (passkeyAuthenticator.isAvailable()) add(LoginMethod.Passkey)
    }
}

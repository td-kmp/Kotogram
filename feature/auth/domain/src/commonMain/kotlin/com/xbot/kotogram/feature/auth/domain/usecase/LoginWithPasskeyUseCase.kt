package com.xbot.kotogram.feature.auth.domain.usecase

import com.xbot.kotogram.feature.auth.domain.repository.PasskeyAuthenticator
import com.xbot.kotogram.feature.auth.domain.repository.PasskeyRepository

class LoginWithPasskeyUseCase(
    private val passkeyRepository: PasskeyRepository,
    private val passkeyAuthenticator: PasskeyAuthenticator
) {
    suspend operator fun invoke() {
        val credential = passkeyAuthenticator.authenticate(passkeyRepository.requestJson())
        passkeyRepository.checkCredential(credential)
    }
}

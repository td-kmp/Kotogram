package com.xbot.kotogram.feature.auth.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.repository.PasskeyAuthenticator
import com.xbot.kotogram.feature.auth.domain.repository.PasskeyRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Scope

@Scope(AccountId::class)
@Factory
class LoginWithPasskeyUseCase(
    private val passkeyRepository: PasskeyRepository,
    private val passkeyAuthenticator: PasskeyAuthenticator
) {
    suspend operator fun invoke(): Either<AuthError, Unit> = either {
        val requestJson = passkeyRepository.getAuthenticationPasskeyParameters().bind()
        val credential = passkeyAuthenticator.authenticate(requestJson).bind()
        passkeyRepository.checkAuthenticationPasskey(credential).bind()
    }
}

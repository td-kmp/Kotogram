package com.xbot.kotogram.feature.auth.domain.usecase

import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import io.xbot.tdlib.api.AuthorizationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Scope

@Scope(AccountId::class)
@Factory
class ObserveAuthorizationStateUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<AuthorizationState> =
        authRepository.authorizationState.distinctUntilChanged()
}

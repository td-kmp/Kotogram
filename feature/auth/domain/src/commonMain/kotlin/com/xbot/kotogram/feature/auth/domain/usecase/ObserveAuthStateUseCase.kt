package com.xbot.kotogram.feature.auth.domain.usecase

import com.xbot.kotogram.feature.auth.domain.model.AuthState
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class ObserveAuthStateUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<AuthState> = authRepository.authState.distinctUntilChanged()
}

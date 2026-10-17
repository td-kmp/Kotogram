package com.xbot.kotogram.feature.auth.presentation.screen.emailcode

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.core.domain.model.AuthorizationState
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.domain.usecase.ObserveAuthorizationStateUseCase
import kotlinx.coroutines.flow.filterIsInstance
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Scope
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

@Scope(AccountId::class)
@KoinViewModel
internal class EmailCodeViewModel(
    private val observeAuthorizationState: ObserveAuthorizationStateUseCase,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<EmailCodeScreenState, EmailCodeScreenState, Nothing> {

    override val container = orbitContainer<EmailCodeScreenState, Nothing>(EmailCodeScreenState()) {
        observeAuthorizationState()
            .filterIsInstance<AuthorizationState.WaitEmailCode>()
            .collect { authState ->
                reduce {
                    state.copy(
                        emailPattern = authState.codeInfo.emailAddressPattern,
                        codeLength = authState.codeInfo.length
                    )
                }
            }
    }

    fun onAction(action: EmailCodeScreenAction) {
        when (action) {
            is EmailCodeScreenAction.OnCodeChange -> onCodeChange(action.value)
            EmailCodeScreenAction.OnSubmit -> onSubmit()
        }
    }

    private fun onCodeChange(value: String) = intent {
        val length = state.codeLength
        val code = value.filter(Char::isDigit).let { if (length > 0) it.take(length) else it }
        reduce { state.copy(code = code, error = null) }
        if (length > 0 && code.length == length) onSubmit()
    }

    private fun onSubmit() = intent {
        if (state.code.isEmpty() || state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        val error = authRepository.checkAuthenticationEmailCode(state.code).leftOrNull()
        reduce {
            state.copy(
                isSubmitting = false,
                error = error,
                code = if (error !=
                    null
                ) {
                    ""
                } else {
                    state.code
                }
            )
        }
    }
}

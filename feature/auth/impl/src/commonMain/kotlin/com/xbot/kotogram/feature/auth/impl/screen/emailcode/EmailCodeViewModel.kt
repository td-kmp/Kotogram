package com.xbot.kotogram.feature.auth.impl.screen.emailcode

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import io.xbot.tdlib.api.AuthorizationStateWaitEmailCode
import kotlinx.coroutines.flow.filterIsInstance
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class EmailCodeViewModel(private val authRepository: AuthRepository) :
    ViewModel(),
    OrbitContainerHost<EmailCodeScreenState, EmailCodeScreenState, Nothing> {

    override val container = orbitContainer<EmailCodeScreenState, Nothing>(EmailCodeScreenState()) {
        authRepository.authorizationState
            .filterIsInstance<AuthorizationStateWaitEmailCode>()
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
                code = if (error != null) "" else state.code
            )
        }
    }
}

package com.xbot.kotogram.feature.auth.impl.screen.password

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import io.xbot.tdlib.api.AuthorizationStateWaitPassword
import kotlinx.coroutines.flow.filterIsInstance
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class PasswordViewModel(private val authRepository: AuthRepository) :
    ViewModel(),
    OrbitContainerHost<PasswordScreenState, PasswordScreenState, Nothing> {

    override val container = orbitContainer<PasswordScreenState, Nothing>(PasswordScreenState()) {
        authRepository.authorizationState
            .filterIsInstance<AuthorizationStateWaitPassword>()
            .collect { authState ->
                reduce {
                    state.copy(
                        hint = authState.passwordHint,
                        hasRecoveryEmail = authState.hasRecoveryEmailAddress,
                        recoveryEmailPattern = authState.recoveryEmailAddressPattern
                    )
                }
            }
    }

    fun onAction(action: PasswordScreenAction) {
        when (action) {
            is PasswordScreenAction.OnPasswordChange -> onPasswordChange(action.value)
            is PasswordScreenAction.OnRecoveryCodeChange -> onRecoveryCodeChange(action.value)
            PasswordScreenAction.OnSubmit -> onSubmit()
            PasswordScreenAction.OnForgotPassword -> onForgotPassword()
        }
    }

    private fun onPasswordChange(value: String) = intent {
        reduce { state.copy(password = value, error = null) }
    }

    private fun onRecoveryCodeChange(value: String) = intent {
        reduce { state.copy(recoveryCode = value.filter(Char::isDigit), error = null) }
    }

    private fun onSubmit() = intent {
        if (state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        val result = if (state.isRecovering) {
            authRepository.recoverAuthenticationPassword(state.recoveryCode)
        } else {
            authRepository.checkAuthenticationPassword(state.password)
        }
        reduce { state.copy(isSubmitting = false, error = result.leftOrNull()) }
    }

    private fun onForgotPassword() = intent {
        if (!state.hasRecoveryEmail) return@intent
        authRepository.requestAuthenticationPasswordRecovery().fold(
            ifLeft = { error -> reduce { state.copy(error = error) } },
            ifRight = { reduce { state.copy(isRecovering = true, error = null) } }
        )
    }
}

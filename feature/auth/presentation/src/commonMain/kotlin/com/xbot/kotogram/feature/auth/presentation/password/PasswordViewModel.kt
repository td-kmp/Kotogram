package com.xbot.kotogram.feature.auth.presentation.password

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.AuthException
import com.xbot.kotogram.feature.auth.domain.model.AuthState
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.domain.usecase.ObserveAuthStateUseCase
import kotlinx.coroutines.flow.filterIsInstance
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

data class PasswordState(
    val hint: String = "",
    val hasRecoveryEmail: Boolean = false,
    val recoveryEmailPattern: String = "",
    val password: String = "",
    /** Recovery code was sent to [recoveryEmailPattern]; the screen asks for it instead of the password. */
    val isRecovering: Boolean = false,
    val recoveryCode: String = "",
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
)

class PasswordViewModel(
    private val observeAuthState: ObserveAuthStateUseCase,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<PasswordState, PasswordState, Nothing> {

    override val container = orbitContainer<PasswordState, Nothing>(PasswordState()) {
        observeAuthState().filterIsInstance<AuthState.WaitPassword>().collect { authState ->
            reduce {
                state.copy(
                    hint = authState.hint,
                    hasRecoveryEmail = authState.hasRecoveryEmail,
                    recoveryEmailPattern = authState.recoveryEmailPattern
                )
            }
        }
    }

    fun onPasswordChange(value: String) = intent {
        reduce { state.copy(password = value, error = null) }
    }

    fun onRecoveryCodeChange(value: String) = intent {
        reduce { state.copy(recoveryCode = value.filter(Char::isDigit), error = null) }
    }

    fun onSubmit() = intent {
        if (state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        try {
            if (state.isRecovering) {
                authRepository.recoverPassword(state.recoveryCode)
            } else {
                authRepository.checkPassword(state.password)
            }
        } catch (e: AuthException) {
            reduce { state.copy(error = e.error) }
        } finally {
            reduce { state.copy(isSubmitting = false) }
        }
    }

    fun onForgotPassword() = intent {
        if (!state.hasRecoveryEmail) return@intent
        try {
            authRepository.requestPasswordRecovery()
            reduce { state.copy(isRecovering = true, error = null) }
        } catch (e: AuthException) {
            reduce { state.copy(error = e.error) }
        }
    }
}

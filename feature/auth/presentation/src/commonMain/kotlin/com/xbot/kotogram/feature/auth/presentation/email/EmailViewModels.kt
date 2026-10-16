package com.xbot.kotogram.feature.auth.presentation.email

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.AuthException
import com.xbot.kotogram.feature.auth.domain.model.AuthState
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.domain.usecase.ObserveAuthStateUseCase
import kotlinx.coroutines.flow.filterIsInstance
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

data class EmailAddressState(
    val email: String = "",
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
)

class EmailAddressViewModel(private val authRepository: AuthRepository) :
    ViewModel(),
    OrbitContainerHost<EmailAddressState, EmailAddressState, Nothing> {

    override val container = orbitContainer<EmailAddressState, Nothing>(EmailAddressState())

    fun onEmailChange(value: String) = intent {
        reduce { state.copy(email = value.trim(), error = null) }
    }

    fun onSubmit() = intent {
        if (state.email.isEmpty() || state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        try {
            authRepository.setEmailAddress(state.email)
        } catch (e: AuthException) {
            reduce { state.copy(error = e.error) }
        } finally {
            reduce { state.copy(isSubmitting = false) }
        }
    }
}

data class EmailCodeState(
    val emailPattern: String = "",
    val codeLength: Int = 0,
    val code: String = "",
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
)

class EmailCodeViewModel(
    private val observeAuthState: ObserveAuthStateUseCase,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<EmailCodeState, EmailCodeState, Nothing> {

    override val container = orbitContainer<EmailCodeState, Nothing>(EmailCodeState()) {
        observeAuthState().filterIsInstance<AuthState.WaitEmailCode>().collect { authState ->
            reduce {
                state.copy(emailPattern = authState.emailPattern, codeLength = authState.codeLength)
            }
        }
    }

    fun onCodeChange(value: String) = intent {
        val length = state.codeLength
        val code = value.filter(Char::isDigit).let { if (length > 0) it.take(length) else it }
        reduce { state.copy(code = code, error = null) }
        if (length > 0 && code.length == length) onSubmit()
    }

    fun onSubmit() = intent {
        if (state.code.isEmpty() || state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        try {
            authRepository.checkEmailCode(state.code)
        } catch (e: AuthException) {
            reduce { state.copy(error = e.error, code = "") }
        } finally {
            reduce { state.copy(isSubmitting = false) }
        }
    }
}

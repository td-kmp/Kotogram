package com.xbot.kotogram.feature.auth.presentation.registration

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.AuthException
import com.xbot.kotogram.feature.auth.domain.model.AuthState
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.domain.usecase.ObserveAuthStateUseCase
import kotlinx.coroutines.flow.filterIsInstance
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

data class RegistrationState(
    val firstName: String = "",
    val lastName: String = "",
    val termsOfService: String = "",
    val isTermsVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
) {
    val canSubmit: Boolean get() = firstName.isNotBlank() && !isSubmitting
}

class RegistrationViewModel(
    private val observeAuthState: ObserveAuthStateUseCase,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<RegistrationState, RegistrationState, Nothing> {

    override val container = orbitContainer<RegistrationState, Nothing>(RegistrationState()) {
        observeAuthState().filterIsInstance<AuthState.WaitRegistration>().collect { authState ->
            reduce { state.copy(termsOfService = authState.termsOfService) }
        }
    }

    fun onFirstNameChange(value: String) = intent {
        reduce { state.copy(firstName = value, error = null) }
    }

    fun onLastNameChange(value: String) = intent {
        reduce { state.copy(lastName = value, error = null) }
    }

    fun onTermsVisibilityChange(visible: Boolean) = intent {
        reduce { state.copy(isTermsVisible = visible) }
    }

    fun onSubmit() = intent {
        if (!state.canSubmit) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        try {
            authRepository.register(state.firstName.trim(), state.lastName.trim())
        } catch (e: AuthException) {
            reduce { state.copy(error = e.error) }
        } finally {
            reduce { state.copy(isSubmitting = false) }
        }
    }
}

package com.xbot.kotogram.feature.auth.presentation.screen.registration

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
internal class RegistrationViewModel(
    private val observeAuthorizationState: ObserveAuthorizationStateUseCase,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<RegistrationScreenState, RegistrationScreenState, Nothing> {

    override val container =
        orbitContainer<RegistrationScreenState, Nothing>(RegistrationScreenState()) {
            observeAuthorizationState()
                .filterIsInstance<AuthorizationState.WaitRegistration>()
                .collect { authState ->
                    reduce { state.copy(termsOfService = authState.termsOfService.text.text) }
                }
        }

    fun onAction(action: RegistrationScreenAction) {
        when (action) {
            is RegistrationScreenAction.OnFirstNameChange -> onFirstNameChange(action.value)

            is RegistrationScreenAction.OnLastNameChange -> onLastNameChange(action.value)

            is RegistrationScreenAction.OnTermsVisibilityChange -> onTermsVisibilityChange(
                action.visible
            )

            RegistrationScreenAction.OnSubmit -> onSubmit()
        }
    }

    private fun onFirstNameChange(value: String) = intent {
        reduce { state.copy(firstName = value, error = null) }
    }

    private fun onLastNameChange(value: String) = intent {
        reduce { state.copy(lastName = value, error = null) }
    }

    private fun onTermsVisibilityChange(visible: Boolean) = intent {
        reduce { state.copy(isTermsVisible = visible) }
    }

    private fun onSubmit() = intent {
        if (!state.canSubmit) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        val error = authRepository.registerUser(
            state.firstName.trim(),
            state.lastName.trim()
        ).leftOrNull()
        reduce { state.copy(isSubmitting = false, error = error) }
    }
}

package com.xbot.kotogram.feature.auth.impl.screen.emailaddress

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Scope
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

@Scope(AccountId::class)
@KoinViewModel
internal class EmailAddressViewModel(private val authRepository: AuthRepository) :
    ViewModel(),
    OrbitContainerHost<EmailAddressScreenState, EmailAddressScreenState, Nothing> {

    override val container =
        orbitContainer<EmailAddressScreenState, Nothing>(EmailAddressScreenState())

    fun onAction(action: EmailAddressScreenAction) {
        when (action) {
            is EmailAddressScreenAction.OnEmailChange -> onEmailChange(action.value)
            EmailAddressScreenAction.OnSubmit -> onSubmit()
        }
    }

    private fun onEmailChange(value: String) = intent {
        reduce { state.copy(email = value.trim(), error = null) }
    }

    private fun onSubmit() = intent {
        if (state.email.isEmpty() || state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        val error = authRepository.setAuthenticationEmailAddress(state.email).leftOrNull()
        reduce { state.copy(isSubmitting = false, error = error) }
    }
}

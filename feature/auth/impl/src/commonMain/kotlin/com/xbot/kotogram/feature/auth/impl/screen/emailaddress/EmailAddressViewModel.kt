package com.xbot.kotogram.feature.auth.impl.screen.emailaddress

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.drop
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class EmailAddressViewModel(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel(),
    OrbitContainerHost<EmailAddressScreenState, EmailAddressScreenState, Nothing> {

    @OptIn(SavedStateHandleSaveableApi::class)
    val email by savedStateHandle.saveable(saver = TextFieldState.Saver) { TextFieldState() }

    override val container =
        orbitContainer<EmailAddressScreenState, Nothing>(EmailAddressScreenState()) {
            snapshotFlow {
                email.text.toString()
            }.drop(1).collect { reduce { state.copy(error = null) } }
        }

    fun onAction(action: EmailAddressScreenAction) {
        when (action) {
            EmailAddressScreenAction.OnSubmit -> onSubmit()
        }
    }

    private fun onSubmit() = intent {
        val address = email.text.toString().trim()
        if (address.isEmpty() || state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        val error = authRepository.setAuthenticationEmailAddress(address).leftOrNull()
        reduce { state.copy(isSubmitting = false, error = error) }
    }
}

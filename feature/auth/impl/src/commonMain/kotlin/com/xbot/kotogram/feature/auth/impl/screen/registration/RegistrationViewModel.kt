package com.xbot.kotogram.feature.auth.impl.screen.registration

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import io.xbot.tdlib.api.AuthorizationStateWaitRegistration
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class RegistrationViewModel(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<RegistrationScreenState, RegistrationScreenState, Nothing> {

    @OptIn(SavedStateHandleSaveableApi::class)
    val firstName by savedStateHandle.saveable(saver = TextFieldState.Saver) { TextFieldState() }

    @OptIn(SavedStateHandleSaveableApi::class)
    val lastName by savedStateHandle.saveable(saver = TextFieldState.Saver) { TextFieldState() }

    var photo: ByteArray? by mutableStateOf(null)
        private set

    override val container =
        orbitContainer<RegistrationScreenState, Nothing>(RegistrationScreenState()) {
            coroutineScope {
                launch { followInput() }
                authRepository.authorizationState
                    .filterIsInstance<AuthorizationStateWaitRegistration>()
                    .collect { authState ->
                        reduce { state.copy(termsOfService = authState.termsOfService.text.text) }
                    }
            }
        }

    private suspend fun Syntax<RegistrationScreenState, Nothing>.followInput() {
        merge(
            snapshotFlow { firstName.text.toString() }.drop(1),
            snapshotFlow { lastName.text.toString() }.drop(1)
        ).collect { reduce { state.copy(error = null) } }
    }

    fun onAction(action: RegistrationScreenAction) {
        when (action) {
            is RegistrationScreenAction.OnTermsVisibilityChange -> onTermsVisibilityChange(
                action.visible
            )

            is RegistrationScreenAction.OnPhotoPicked -> photo = action.jpeg

            RegistrationScreenAction.OnSubmit -> onSubmit()
        }
    }

    private fun onTermsVisibilityChange(visible: Boolean) = intent {
        reduce { state.copy(isTermsVisible = visible) }
    }

    private fun onSubmit() = intent {
        val first = firstName.text.toString().trim()
        if (first.isEmpty() || state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        val error = authRepository.registerUser(first, lastName.text.toString().trim(), photo).leftOrNull()
        reduce { state.copy(isSubmitting = false, error = error) }
    }
}

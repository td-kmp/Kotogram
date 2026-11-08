package com.xbot.kotogram.feature.auth.impl.screen.emailcode

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import io.xbot.tdlib.api.AuthorizationStateWaitEmailCode
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class EmailCodeViewModel(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<EmailCodeScreenState, EmailCodeScreenState, Nothing> {

    @OptIn(SavedStateHandleSaveableApi::class)
    val code by savedStateHandle.saveable(saver = TextFieldState.Saver) { TextFieldState() }

    override val container = orbitContainer<EmailCodeScreenState, Nothing>(EmailCodeScreenState()) {
        coroutineScope {
            launch { followInput() }
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
    }

    private suspend fun Syntax<EmailCodeScreenState, Nothing>.followInput() {
        snapshotFlow { code.text.toString() }.drop(1).collect { value ->
            if (value.isNotEmpty()) reduce { state.copy(error = null) }
            if (state.codeLength > 0 && value.length == state.codeLength) onSubmit()
        }
    }

    fun onAction(action: EmailCodeScreenAction) {
        when (action) {
            EmailCodeScreenAction.OnSubmit -> onSubmit()
        }
    }

    private fun onSubmit() = intent {
        val value = code.text.toString()
        if (value.isEmpty() || state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        val error = authRepository.checkAuthenticationEmailCode(value).leftOrNull()
        if (error != null) code.clearText()
        reduce { state.copy(isSubmitting = false, error = error) }
    }
}

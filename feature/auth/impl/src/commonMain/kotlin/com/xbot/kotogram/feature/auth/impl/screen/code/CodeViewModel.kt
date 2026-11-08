package com.xbot.kotogram.feature.auth.impl.screen.code

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.xbot.kotogram.feature.auth.domain.extensions.length
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import io.xbot.tdlib.api.AuthorizationStateWaitCode
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class CodeViewModel(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<CodeScreenState, CodeScreenState, Nothing> {

    @OptIn(SavedStateHandleSaveableApi::class)
    val code by savedStateHandle.saveable(saver = TextFieldState.Saver) { TextFieldState() }

    override val container = orbitContainer<CodeScreenState, Nothing>(CodeScreenState()) {
        coroutineScope {
            launch { followInput() }
            authRepository.authorizationState
                .filterIsInstance<AuthorizationStateWaitCode>()
                .collectLatest { authState ->
                    code.clearText()
                    reduce {
                        state.copy(
                            info = authState.codeInfo,
                            resendSecondsLeft = authState.codeInfo.timeout
                        )
                    }
                    while (state.resendSecondsLeft > 0) {
                        delay(TICK_MILLIS)
                        reduce { state.copy(resendSecondsLeft = state.resendSecondsLeft - 1) }
                    }
                }
        }
    }

    // A full code goes by itself, as in Telegram's apps. Typing clears an error; the field cleared after one doesn't.
    private suspend fun Syntax<CodeScreenState, Nothing>.followInput() {
        snapshotFlow { code.text.toString() }.drop(1).collect { value ->
            if (value.isNotEmpty()) reduce { state.copy(error = null) }
            val length = state.info?.type?.length ?: 0
            if (length > 0 && value.length == length) submit()
        }
    }

    fun onAction(action: CodeScreenAction) {
        when (action) {
            CodeScreenAction.OnSubmit -> submit()
            CodeScreenAction.OnResend -> onResend()
        }
    }

    private fun onResend() = intent {
        if (!state.canResend) return@intent
        authRepository.resendAuthenticationCode().onLeft { error ->
            reduce { state.copy(error = error) }
        }
    }

    private fun submit() = intent {
        val value = code.text.toString()
        if (value.isEmpty() || state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        val error = authRepository.checkAuthenticationCode(value).leftOrNull()
        if (error != null) code.clearText()
        reduce { state.copy(isSubmitting = false, error = error) }
    }

    private companion object {
        const val TICK_MILLIS = 1_000L
    }
}

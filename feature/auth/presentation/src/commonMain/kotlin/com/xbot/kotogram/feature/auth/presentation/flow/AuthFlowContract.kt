package com.xbot.kotogram.feature.auth.presentation.flow

import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.AuthState
import com.xbot.kotogram.feature.auth.domain.model.LoginMethod

data class AuthFlowState(
    val authState: AuthState = AuthState.Initializing,
    /** The first screen of the flow: QR on desktop by default, phone number elsewhere. */
    val entryMethod: LoginMethod,
    val loginMethods: Set<LoginMethod> = emptySet(),
    /** The user went back from a step to the entry screen to change the number or method. */
    val isEditingEntry: Boolean = false,
    val isPasskeyInProgress: Boolean = false
) {
    val backStack: List<AuthRoute>
        get() {
            val entry = if (entryMethod == LoginMethod.QrCode) AuthRoute.Qr else AuthRoute.Phone
            val step = when (authState) {
                is AuthState.WaitCode -> AuthRoute.Code

                is AuthState.WaitEmailAddress -> AuthRoute.EmailAddress

                is AuthState.WaitEmailCode -> AuthRoute.EmailCode

                is AuthState.WaitPassword -> AuthRoute.Password

                is AuthState.WaitRegistration -> AuthRoute.Registration

                is AuthState.Unsupported -> AuthRoute.Unsupported

                // TDLib restarts (QR -> phone switch) and the hand-off to the main screen keep the entry screen.
                AuthState.WaitPhoneNumber,
                is AuthState.WaitQrConfirmation,
                AuthState.Initializing,
                AuthState.Ready,
                AuthState.LoggingOut
                -> null
            }
            return listOfNotNull(entry, step.takeUnless { isEditingEntry })
        }
}

sealed interface AuthFlowSideEffect {
    data class ShowError(val error: AuthError) : AuthFlowSideEffect
}

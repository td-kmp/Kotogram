package com.xbot.kotogram.feature.auth.presentation.screen.auth

import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.LoginMethod
import com.xbot.kotogram.feature.auth.presentation.navigation.AuthStepRoute

/** State shared by every screen of the login flow. */
internal data class AuthScreenState(
    /** The first screen: QR on desktop, phone number elsewhere. */
    val entryMethod: LoginMethod,
    val loginMethods: Set<LoginMethod> = emptySet(),
    val isPasskeyInProgress: Boolean = false,
    val error: AuthError? = null
)

internal sealed interface AuthScreenAction {
    data object OnPasskeyLoginClick : AuthScreenAction

    /** An entry screen submitted again; show the current step even if TDLib's state didn't change. */
    data object OnEntrySubmitted : AuthScreenAction
}

internal sealed interface AuthScreenSideEffect {
    /** Replace the steps on top of the entry screens with [route]. */
    data class ShowStep(val route: AuthStepRoute) : AuthScreenSideEffect

    /** Back to the entry screens: TDLib waits for a phone number or a QR confirmation again. */
    data object ClearSteps : AuthScreenSideEffect
}

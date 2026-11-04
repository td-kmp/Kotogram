package com.xbot.kotogram.feature.auth.presentation.screen.auth

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.feature.auth.domain.model.LoginMethod
import com.xbot.kotogram.feature.auth.domain.repository.LoginCapabilities
import com.xbot.kotogram.feature.auth.domain.usecase.GetLoginMethodsUseCase
import com.xbot.kotogram.feature.auth.domain.usecase.LoginWithPasskeyUseCase
import com.xbot.kotogram.feature.auth.domain.usecase.ObserveAuthorizationStateUseCase
import com.xbot.kotogram.feature.auth.presentation.navigation.AuthStepRoute
import com.xbot.kotogram.feature.auth.presentation.navigation.CodeRoute
import com.xbot.kotogram.feature.auth.presentation.navigation.EmailAddressRoute
import com.xbot.kotogram.feature.auth.presentation.navigation.EmailCodeRoute
import com.xbot.kotogram.feature.auth.presentation.navigation.PasswordRoute
import com.xbot.kotogram.feature.auth.presentation.navigation.PremiumPurchaseRoute
import com.xbot.kotogram.feature.auth.presentation.navigation.RegistrationRoute
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateWaitCode
import io.xbot.tdlib.api.AuthorizationStateWaitEmailAddress
import io.xbot.tdlib.api.AuthorizationStateWaitEmailCode
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.AuthorizationStateWaitPassword
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import io.xbot.tdlib.api.AuthorizationStateWaitPremiumPurchase
import io.xbot.tdlib.api.AuthorizationStateWaitRegistration
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Scope
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer

/**
 * Shared by every entry of the login flow (they declare [com.xbot.kotogram.feature.auth.api.AuthRoute]
 * as their parent). Routes between the steps from TDLib's authorization state.
 */
@Scope(AccountId::class)
@KoinViewModel
internal class AuthViewModel(
    loginCapabilities: LoginCapabilities,
    private val observeAuthorizationState: ObserveAuthorizationStateUseCase,
    private val getLoginMethods: GetLoginMethodsUseCase,
    private val loginWithPasskey: LoginWithPasskeyUseCase
) : ViewModel(),
    OrbitContainerHost<AuthScreenState, AuthScreenState, AuthScreenSideEffect> {

    private var authorizationState: AuthorizationState? = null

    override val container = orbitContainer<AuthScreenState, AuthScreenSideEffect>(
        AuthScreenState(
            entryMethod = if (loginCapabilities.supportsQrLogin) {
                LoginMethod.QrCode
            } else {
                LoginMethod.PhoneNumber
            }
        )
    ) {
        val loginMethods = getLoginMethods()
        reduce { state.copy(loginMethods = loginMethods) }
        observeAuthorizationState().collect { authorizationState ->
            this@AuthViewModel.authorizationState = authorizationState
            route(authorizationState)
        }
    }

    fun onAction(action: AuthScreenAction) {
        when (action) {
            AuthScreenAction.OnPasskeyLoginClick -> onPasskeyLoginClick()
            AuthScreenAction.OnEntrySubmitted -> onEntrySubmitted()
        }
    }

    private fun onPasskeyLoginClick() = intent {
        reduce { state.copy(isPasskeyInProgress = true, error = null) }
        val error = loginWithPasskey().leftOrNull()
        reduce { state.copy(isPasskeyInProgress = false, error = error) }
    }

    private fun onEntrySubmitted() = intent {
        authorizationState?.let { route(it) }
    }

    // Routing on transitions keeps back working: back from a step shows the entry screen again,
    // and TDLib accepts a new phone number from any step.
    private suspend fun Syntax<AuthScreenState, AuthScreenSideEffect>.route(
        state: AuthorizationState
    ) {
        when (state) {
            AuthorizationStateWaitPhoneNumber,
            is AuthorizationStateWaitOtherDeviceConfirmation -> postSideEffect(
                AuthScreenSideEffect.ClearSteps
            )

            else -> state.stepRoute()?.let { postSideEffect(AuthScreenSideEffect.ShowStep(it)) }
        }
    }

    private fun AuthorizationState.stepRoute(): AuthStepRoute? = when (this) {
        is AuthorizationStateWaitCode -> CodeRoute
        is AuthorizationStateWaitPassword -> PasswordRoute
        is AuthorizationStateWaitEmailAddress -> EmailAddressRoute
        is AuthorizationStateWaitEmailCode -> EmailCodeRoute
        is AuthorizationStateWaitRegistration -> RegistrationRoute
        is AuthorizationStateWaitPremiumPurchase -> PremiumPurchaseRoute
        else -> null
    }
}

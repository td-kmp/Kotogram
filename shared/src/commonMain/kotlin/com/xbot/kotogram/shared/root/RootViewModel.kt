package com.xbot.kotogram.shared.root

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.core.domain.model.AuthorizationState
import com.xbot.kotogram.core.domain.repository.AccountRepository
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.feature.auth.api.AuthRoute
import com.xbot.kotogram.feature.chats.api.ChatListRoute
import org.koin.core.annotation.KoinViewModel
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

/** The active account and the root of its back stack, derived from its TDLib authorization state. */
internal data class RootScreenState(
    val accountId: AccountId? = null,
    val route: NavKey = SplashRoute
)

@KoinViewModel
internal class RootViewModel(private val accountRepository: AccountRepository) :
    ViewModel(),
    OrbitContainerHost<RootScreenState, RootScreenState, Nothing> {

    private var previous: AuthorizationState? = null

    override val container = orbitContainer<RootScreenState, Nothing>(RootScreenState()) {
        accountRepository.active.collect { account ->
            val authorizationState = account?.authorizationState
            val sameAccount = account?.id == state.accountId
            val route = when (authorizationState) {
                null -> SplashRoute

                AuthorizationState.Ready -> ChatListRoute

                // A restart inside the login flow (leaving QR login) keeps the flow on screen; a real
                // log out (after Ready) shows the splash until TDLib is ready for a new login.
                AuthorizationState.WaitTdlibParameters,
                AuthorizationState.LoggingOut,
                AuthorizationState.Closing,
                AuthorizationState.Closed -> when {
                    !sameAccount -> SplashRoute
                    previous == AuthorizationState.Ready -> SplashRoute
                    else -> state.route
                }

                else -> AuthRoute
            }
            previous = authorizationState
            reduce { RootScreenState(accountId = account?.id, route = route) }
        }
    }
}

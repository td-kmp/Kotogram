package com.xbot.kotogram.shared.root

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.core.model.extensions.isLoginStep
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.tdlib.account.AccountRepository
import com.xbot.kotogram.feature.auth.api.AuthRoute
import com.xbot.kotogram.feature.chats.api.ChatListRoute
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateReady
import org.koin.core.annotation.KoinViewModel
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

/** The active account and the root of its back stack, derived from its TDLib authorization state. */
internal data class RootScreenState(
    val accountId: AccountId? = null,
    val route: NavKey = SplashRoute
)

internal sealed interface RootScreenAction {
    data class OnAccountShown(val accountId: AccountId?) : RootScreenAction
}

@KoinViewModel
internal class RootViewModel(private val accountRepository: AccountRepository) :
    ViewModel(),
    OrbitContainerHost<RootScreenState, RootScreenState, Nothing> {

    override val container = orbitContainer<RootScreenState, Nothing>(RootScreenState()) {
        accountRepository.active.collect { account ->
            reduce {
                RootScreenState(
                    accountId = account?.id,
                    route = routeOf(
                        authorizationState = account?.authorizationState,
                        current = state.route,
                        sameAccount = account?.id == state.accountId
                    )
                )
            }
        }
    }

    fun onAction(action: RootScreenAction) {
        when (action) {
            is RootScreenAction.OnAccountShown -> accountRepository.onAccountShown(action.accountId)
        }
    }
}

internal fun routeOf(
    authorizationState: AuthorizationState?,
    current: NavKey,
    sameAccount: Boolean
): NavKey = when {
    authorizationState == AuthorizationStateReady -> ChatListRoute

    authorizationState.isLoginStep -> AuthRoute

    // A restart inside the login flow (leaving QR login) keeps the flow on screen.
    sameAccount && current == AuthRoute -> AuthRoute

    else -> SplashRoute
}

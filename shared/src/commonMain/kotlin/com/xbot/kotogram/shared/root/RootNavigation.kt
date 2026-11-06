package com.xbot.kotogram.shared.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.xbot.kotogram.core.account.AccountManager
import com.xbot.kotogram.core.account.AccountSession
import com.xbot.kotogram.core.account.extensions.isLoginStep
import com.xbot.kotogram.core.navigation.LocalNavigator
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.koinNavSerializersModule
import com.xbot.kotogram.core.navigation.rememberNavigator
import com.xbot.kotogram.core.navigation.rememberSharedViewModelStoreNavEntryDecorator
import com.xbot.kotogram.feature.auth.api.AuthRoute
import com.xbot.kotogram.feature.chats.api.ChatListRoute
import com.xbot.kotogram.shared.navigation.SplashRoute
import com.xbot.kotogram.shared.screen.splash.SplashScreen
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateReady
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.compose.scope.UnboundKoinScope
import org.koin.core.annotation.KoinDelicateAPI
import org.koin.core.annotation.KoinExperimentalAPI

@Composable
internal fun RootNavigation(modifier: Modifier = Modifier) {
    val manager = koinInject<AccountManager>()
    val shown by manager.shown.collectAsStateWithLifecycle()
    Surface(modifier.fillMaxSize()) {
        val active = shown.active
        if (active == null) SplashScreen() else Account(active)
        shown.adding?.let { Account(it, onRootBack = manager::cancelAdding) }
    }
}

// Not KoinScope { } or rememberKoinScope: they close the scope with the composition, and an account runs without UI.
@OptIn(KoinExperimentalAPI::class, KoinDelicateAPI::class)
@Composable
private fun Account(session: AccountSession, onRootBack: (() -> Unit)? = null) {
    // ViewModel stores hang on this position in the composition, so they stay apart and clear with the account.
    key(session.id) {
        val authorizationState by session.authorizationState.collectAsStateWithLifecycle()
        UnboundKoinScope(session.koinScope) {
            // A Surface of its own: the sign-in of a new account covers the active one and takes its touches.
            Surface(Modifier.fillMaxSize()) {
                AccountNavigation(root = routeOf(authorizationState), onRootBack = onRootBack)
            }
        }
    }
}

private fun routeOf(authorizationState: AuthorizationState?): NavKey = when {
    authorizationState == AuthorizationStateReady -> ChatListRoute
    authorizationState.isLoginStep -> AuthRoute
    else -> SplashRoute
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun AccountNavigation(root: NavKey, onRootBack: (() -> Unit)?) {
    val navigator = rememberNavigator(
        startRoute = SplashRoute,
        serializersModule = koinNavSerializersModule()
    )
    LaunchedEffect(navigator, root) { navigator.setRoot(root) }
    // Before NavDisplay: back handlers are LIFO, so the stacks inside it, the login flow's too, go first.
    if (onRootBack != null) {
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            isBackEnabled = navigator.backStack.size <= 1,
            onBackCompleted = onRootBack
        )
    }
    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavDisplay(
            backStack = navigator.backStack,
            onBack = navigator::navigateBack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberSharedViewModelStoreNavEntryDecorator()
            ),
            entryProvider = koinEntryProvider<NavKey>()
        )
    }
}

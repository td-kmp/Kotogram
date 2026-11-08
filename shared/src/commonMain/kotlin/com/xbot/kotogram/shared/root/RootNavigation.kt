package com.xbot.kotogram.shared.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.xbot.kotogram.core.account.AccountManager
import com.xbot.kotogram.core.account.AccountSession
import com.xbot.kotogram.core.designsystem.animation.rememberNavTransitions
import com.xbot.kotogram.core.navigation.LocalNavigator
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.koinNavSerializersModule
import com.xbot.kotogram.core.navigation.rememberNavigator
import com.xbot.kotogram.core.navigation.rememberSharedViewModelStoreNavEntryDecorator
import com.xbot.kotogram.shared.extensions.toRoute
import com.xbot.kotogram.shared.screen.splash.SplashScreen
import kotlinx.coroutines.flow.mapNotNull
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
        if (active != null) Account(active) else SplashScreen()

        val adding = shown.adding
        if (adding != null) Account(adding, onRootBack = manager::cancelAdding)
    }
}

@OptIn(KoinExperimentalAPI::class, KoinDelicateAPI::class)
@Composable
private fun Account(
    session: AccountSession,
    onRootBack: (() -> Unit)? = null
) {
    key(session.id) {
        // The last screen TDLib decided: a closing account keeps it until its replacement shows.
        val roots = remember(session) { session.authorizationState.mapNotNull { it.toRoute() } }
        val root by roots.collectAsStateWithLifecycle(
            initialValue = session.authorizationState.value.toRoute()
        )

        UnboundKoinScope(session.koinScope) {
            Surface(Modifier.fillMaxSize()) {
                val current = root
                if (current != null) {
                    AccountNavigation(
                        root = current,
                        onRootBack = onRootBack
                    )
                } else {
                    SplashScreen()
                }
            }
        }
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun AccountNavigation(root: NavKey, onRootBack: (() -> Unit)?) {
    val navigator = rememberNavigator(
        startRoute = root,
        serializersModule = koinNavSerializersModule()
    )
    LaunchedEffect(navigator, root) {
        navigator.setRoot(root)
    }

    if (onRootBack != null) {
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            isBackEnabled = navigator.backStack.size <= 1,
            onBackCompleted = onRootBack
        )
    }

    val transitions = rememberNavTransitions<Scene<NavKey>>()
    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavDisplay(
            backStack = navigator.backStack,
            onBack = navigator::navigateBack,
            transitionSpec = transitions.push,
            popTransitionSpec = transitions.pop,
            predictivePopTransitionSpec = transitions.predictivePop,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberSharedViewModelStoreNavEntryDecorator()
            ),
            entryProvider = koinEntryProvider()
        )
    }
}

package com.xbot.kotogram.shared.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.xbot.kotogram.core.navigation.LocalNavigator
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.koinNavSerializersModule
import com.xbot.kotogram.core.navigation.rememberNavigator
import com.xbot.kotogram.core.navigation.rememberSharedViewModelStoreNavEntryDecorator
import com.xbot.kotogram.core.tdlib.accountScope
import org.koin.compose.getKoin
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.compose.scope.UnboundKoinScope
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinDelicateAPI
import org.koin.core.annotation.KoinExperimentalAPI
import org.orbitmvi.orbit.compose.collectAsState

/**
 * Shows the active account. Each account keeps its own back stack (saved per account), resolves its
 * screens from its Koin scope and keeps their ViewModels in its own store.
 */
@OptIn(KoinExperimentalAPI::class, KoinDelicateAPI::class)
@Composable
internal fun RootNavigation(
    modifier: Modifier = Modifier,
    viewModel: RootViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val accountScope = state.accountId?.let(getKoin()::accountScope)
    val saveableStateHolder = rememberSaveableStateHolder()
    Surface(modifier.fillMaxSize()) {
        val accountId = state.accountId
        if (accountId == null || accountScope == null) {
            AccountNavigation(SplashRoute)
        } else {
            saveableStateHolder.SaveableStateProvider(accountId.value) {
                // Tells the repository when the account has really left the screen: only then its scope closes.
                DisposableEffect(accountId) {
                    viewModel.onAction(RootScreenAction.OnAccountShown(accountId))
                    onDispose { viewModel.onAction(RootScreenAction.OnAccountShown(null)) }
                }
                // DefaultAccountRepository closes the scope; composition only provides it.
                UnboundKoinScope(scope = accountScope) {
                    CompositionLocalProvider(
                        LocalViewModelStoreOwner provides
                            accountScope.get<AccountViewModelStoreOwner>()
                    ) {
                        AccountNavigation(state.route)
                    }
                }
            }
        }
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun AccountNavigation(root: NavKey) {
    val navigator =
        rememberNavigator(startRoute = SplashRoute, serializersModule = koinNavSerializersModule())
    LaunchedEffect(navigator, root) { navigator.setRoot(root) }
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

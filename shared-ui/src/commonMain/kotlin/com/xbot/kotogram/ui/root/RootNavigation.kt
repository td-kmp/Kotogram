package com.xbot.kotogram.ui.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.xbot.kotogram.feature.auth.presentation.flow.AuthFlow
import com.xbot.kotogram.ui.ChatListScreen
import com.xbot.kotogram.ui.ChatScreen
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun RootNavigation(modifier: Modifier = Modifier, viewModel: RootViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    Surface(modifier.fillMaxSize()) {
        NavDisplay(
            backStack = listOf(state.route),
            onBack = {},
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<RootRoute.Splash> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                entry<RootRoute.Auth> { AuthFlow() }
                entry<RootRoute.Main> { MainContent() }
            }
        )
    }
}

// Placeholder until the chat feature gets its own module and navigation.
@Composable
private fun MainContent() {
    var openedChat by rememberSaveable { mutableStateOf<Long?>(null) }
    when (val chatId = openedChat) {
        null -> ChatListScreen(onChatClick = { openedChat = it })
        else -> ChatScreen(chatId = chatId, onBack = { openedChat = null })
    }
}

package com.xbot.kotogram.feature.chats.presentation.screen.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.feature.chats.presentation.resources.Res
import com.xbot.kotogram.feature.chats.presentation.resources.chat_back
import com.xbot.kotogram.feature.chats.presentation.resources.chat_history_placeholder
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun ChatScreen(
    chatId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = koinViewModel { parametersOf(chatId) }
) {
    val state by viewModel.collectAsState()
    ChatScreenContent(state = state, onBackClick = onBackClick, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreenContent(
    state: ChatScreenState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) { Text(stringResource(Res.string.chat_back)) }
                }
            )
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.chat_history_placeholder),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@KotogramPreview
@Composable
private fun ChatScreenPreview() {
    ChatScreenContent(state = ChatScreenState(title = "Kotogram"), onBackClick = {})
}

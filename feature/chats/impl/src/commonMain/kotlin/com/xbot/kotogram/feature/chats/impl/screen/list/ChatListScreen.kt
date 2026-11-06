package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.chats_empty
import com.xbot.kotogram.core.resources.chats_load_failed
import com.xbot.kotogram.core.resources.chats_title
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.stringResource
import org.orbitmvi.orbit.compose.collectAsState

private const val END_THRESHOLD = 10

@Composable
internal fun ChatListScreen(
    viewModel: ChatListViewModel,
    onChatClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.collectAsState()
    ChatListScreenContent(
        state = state,
        onAction = viewModel::onAction,
        onChatClick = onChatClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatListScreenContent(
    state: ChatListScreenState,
    onAction: (ChatListScreenAction) -> Unit,
    onChatClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            info.totalItemsCount to (last >= info.totalItemsCount - END_THRESHOLD)
        }
            .distinctUntilChanged()
            .filter { (_, isNearEnd) -> isNearEnd }
            .collect { onAction(ChatListScreenAction.OnEndReached) }
    }
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.chats_title)) }) }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(state.rows, key = { it.chatId }, contentType = { "chat" }) { row ->
                    ChatRow(row = row, onClick = { onChatClick(row.chatId) })
                }
            }
            if (state.rows.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when {
                        state.isLoading -> CircularProgressIndicator()
                        state.loadFailed -> Text(stringResource(Res.string.chats_load_failed))
                        state.isComplete -> Text(stringResource(Res.string.chats_empty))
                    }
                }
            }
        }
    }
}

@KotogramPreview
@Composable
private fun ChatListScreenPreview(
    @PreviewParameter(ChatListScreenStateProvider::class) state: ChatListScreenState
) {
    ChatListScreenContent(state = state, onAction = {}, onChatClick = {})
}

private class ChatListScreenStateProvider : PreviewParameterProvider<ChatListScreenState> {
    private val rows = listOf(
        ChatListRow(
            1,
            "Saved Messages",
            ChatPreview.Text("tdlib-kmp 1.8.69"),
            null,
            "20:14",
            0,
            false,
            true
        ),
        ChatListRow(
            2,
            "Kotogram",
            ChatPreview.Media(MediaKind.Photo, ""),
            "Ada",
            "19:02",
            3,
            false,
            false
        ),
        ChatListRow(3, "Muted group", ChatPreview.Draft("Hello"), null, "02.11", 12, true, false)
    )

    override val values = sequenceOf(
        ChatListScreenState(rows = rows),
        ChatListScreenState(isLoading = true),
        ChatListScreenState(isComplete = true),
        ChatListScreenState(loadFailed = true)
    )
}

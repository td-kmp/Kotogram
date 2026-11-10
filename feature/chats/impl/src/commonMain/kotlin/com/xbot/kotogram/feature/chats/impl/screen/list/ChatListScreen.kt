package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.ProgressIndicator
import com.xbot.kotogram.core.designsystem.component.TopBar
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.annotatedStringResource
import com.xbot.kotogram.core.resources.chatlist_emptytext
import com.xbot.kotogram.core.resources.error_anerror
import com.xbot.kotogram.core.resources.peerlist_title_chats
import com.xbot.kotogram.core.resources.stringResource
import io.xbot.tdlib.api.FormattedText
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.orbitmvi.orbit.compose.collectAsState

private const val END_THRESHOLD = 10

@Composable
internal fun ChatListScreen(
    viewModel: ChatListViewModel,
    onChatClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.collectAsState()
    ChatListScreenContent(
        state = state,
        onAction = viewModel::onAction,
        onChatClick = onChatClick,
        modifier = modifier,
    )
}

@Composable
internal fun ChatListScreenContent(
    state: ChatListScreenState,
    onAction: (ChatListScreenAction) -> Unit,
    onChatClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = rememberStyle(::ChatListScreenStyleScope, ChatListScreenStyle.Adaptive)
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
    context(style) {
        Scaffold(
            modifier = modifier,
            containerColor = KotogramTheme.colors.background,
            topBar = { TopBar(title = { Text(stringResource(Res.string.peerlist_title_chats)) }) },
        ) { innerPadding ->
            Column(Modifier.fillMaxSize().padding(innerPadding)) {
                if (state.tabs.size > 1) {
                    FolderTabs(
                        tabs = state.tabs,
                        selected = state.selected,
                        customEmoji = state.customEmoji,
                        onSelect = { onAction(ChatListScreenAction.OnFolderSelect(it)) },
                    )
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        items(state.rows, key = { it.chatId }, contentType = { "chat" }) { row ->
                            ChatRow(row = row, customEmoji = state.customEmoji, onClick = {
                                onChatClick(row.chatId)
                            })
                        }
                    }
                    if (state.rows.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            when {
                                state.isLoading -> ProgressIndicator()

                                state.loadFailed -> Text(stringResource(Res.string.error_anerror))

                                state.isComplete -> Text(
                                    text = annotatedStringResource(Res.string.chatlist_emptytext),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@KotogramPreview
@Composable
private fun ChatListScreenPreview(@PreviewParameter(ChatListScreenStateProvider::class) state: ChatListScreenState) {
    ChatListScreenContent(state = state, onAction = {}, onChatClick = {})
}

private class ChatListScreenStateProvider : PreviewParameterProvider<ChatListScreenState> {
    override val values = sequenceOf(
        ChatListScreenState(rows = SampleRows),
        ChatListScreenState(isLoading = true),
        ChatListScreenState(isComplete = true),
        ChatListScreenState(loadFailed = true),
    )
}

internal val SampleRows = listOf(
    sampleRow(1, "Anastasia", "And you?", time = "00:23", unread = 5, color = 0),
    sampleRow(
        2,
        "I want to be with you",
        "Hi, it's Nastya. I'm about to turn thirty, I have a husband and a son…",
        time = "Sun",
        muted = true,
        pinned = true,
        color = 2,
    ),
    sampleRow(
        3,
        "Nastya",
        "goodnight",
        time = "14/10",
        pinned = true,
        color = 3,
        outgoing = OutgoingStatus.Read,
    ),
    sampleRow(
        4,
        "Emergency | News 18+",
        "A ten-year-old boy crashed his quad bike",
        time = "01:04",
        unread = 537,
        muted = true,
        color = 0,
        media = MediaKind.Photo,
    ),
    sampleRow(
        5,
        "Live • News",
        "",
        time = "01:04",
        unread = 37,
        muted = true,
        color = 3,
        media = MediaKind.Photo,
    ),
    sampleRow(
        6,
        "Rozetked Discuss",
        "Sure, and some more memory for it…",
        time = "01:02",
        unread = 189_700,
        muted = true,
        color = 1,
        sender = "King Crimson",
    ),
    sampleRow(
        7,
        "Proxy MTProto",
        "Server: Unknown Port: 443 Secret: ee…",
        time = "01:00",
        unread = 6_300,
        muted = true,
        color = 5,
    ),
)

private fun sampleRow(
    id: Long,
    title: String,
    text: String,
    time: String,
    color: Int,
    unread: Int = 0,
    muted: Boolean = false,
    pinned: Boolean = false,
    outgoing: OutgoingStatus? = null,
    media: MediaKind? = null,
    sender: String? = null,
) = ChatListRow(
    chatId = id,
    title = title,
    avatar = AvatarModel(
        photo = null,
        thumbnail = null,
        letters = title.take(1),
        colorIndex = color,
    ),
    emojiStatusId = null,
    isMuted = muted,
    isPinned = pinned,
    time = time,
    outgoing = outgoing,
    unreadCount = unread,
    hasUnreadMention = false,
    isMarkedAsUnread = false,
    senderName = sender,
    preview = if (media != null) {
        ChatPreview.Media(media, FormattedText(text = text), thumbnail = null)
    } else {
        ChatPreview.Text(FormattedText(text = text))
    },
)

package com.xbot.kotogram.feature.chats.presentation.screen.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.feature.chats.presentation.resources.Res
import com.xbot.kotogram.feature.chats.presentation.resources.chats_draft
import com.xbot.kotogram.feature.chats.presentation.resources.chats_empty
import com.xbot.kotogram.feature.chats.presentation.resources.chats_load_failed
import com.xbot.kotogram.feature.chats.presentation.resources.chats_sender
import com.xbot.kotogram.feature.chats.presentation.resources.chats_title
import com.xbot.kotogram.feature.chats.presentation.resources.preview_animation
import com.xbot.kotogram.feature.chats.presentation.resources.preview_audio
import com.xbot.kotogram.feature.chats.presentation.resources.preview_document
import com.xbot.kotogram.feature.chats.presentation.resources.preview_other
import com.xbot.kotogram.feature.chats.presentation.resources.preview_photo
import com.xbot.kotogram.feature.chats.presentation.resources.preview_sticker
import com.xbot.kotogram.feature.chats.presentation.resources.preview_video
import com.xbot.kotogram.feature.chats.presentation.resources.preview_video_note
import com.xbot.kotogram.feature.chats.presentation.resources.preview_voice_note
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

private const val END_THRESHOLD = 10

@Composable
internal fun ChatListScreen(
    onChatClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel = koinViewModel()
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

@Composable
private fun ChatRow(row: ChatListRow, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        leadingContent = { Avatar(row.title) },
        headlineContent = { Text(row.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Text(previewText(row), maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(row.time, style = MaterialTheme.typography.labelSmall)
                if (row.unreadCount > 0) {
                    Badge(
                        containerColor = if (row.isMuted) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            BadgeDefaults.containerColor
                        }
                    ) {
                        Text(row.unreadCount.toString())
                    }
                }
            }
        }
    )
}

@Composable
private fun previewText(row: ChatListRow): String {
    val text = when (val preview = row.preview) {
        ChatPreview.Empty -> ""
        is ChatPreview.Text -> preview.text
        is ChatPreview.Draft -> stringResource(Res.string.chats_draft, preview.text)
        is ChatPreview.Media -> preview.caption.ifEmpty { stringResource(preview.kind.label()) }
        ChatPreview.Other -> stringResource(Res.string.preview_other)
    }
    return row.senderName?.let { stringResource(Res.string.chats_sender, it, text) } ?: text
}

private fun MediaKind.label() = when (this) {
    MediaKind.Photo -> Res.string.preview_photo
    MediaKind.Video -> Res.string.preview_video
    MediaKind.Document -> Res.string.preview_document
    MediaKind.Audio -> Res.string.preview_audio
    MediaKind.VoiceNote -> Res.string.preview_voice_note
    MediaKind.VideoNote -> Res.string.preview_video_note
    MediaKind.Animation -> Res.string.preview_animation
    MediaKind.Sticker -> Res.string.preview_sticker
}

@Composable
private fun Avatar(title: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(title.take(1), style = MaterialTheme.typography.titleMedium)
        }
    }
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

@KotogramPreview
@Composable
private fun ChatListScreenPreview(
    @PreviewParameter(ChatListScreenStateProvider::class) state: ChatListScreenState
) {
    ChatListScreenContent(state = state, onAction = {}, onChatClick = {})
}

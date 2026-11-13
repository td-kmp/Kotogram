package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.PreviewKotogram
import com.xbot.kotogram.core.designsystem.component.BarAction
import com.xbot.kotogram.core.designsystem.component.BarTextAction
import com.xbot.kotogram.core.designsystem.component.ChatListItemSeparator
import com.xbot.kotogram.core.designsystem.component.MessageStatus
import com.xbot.kotogram.core.designsystem.component.ProgressIndicator
import com.xbot.kotogram.core.designsystem.component.SearchScaffold
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.StringResource
import com.xbot.kotogram.core.resources.annotatedStringResource
import com.xbot.kotogram.core.resources.chat_list_photo1
import com.xbot.kotogram.core.resources.chatlist_emptytext
import com.xbot.kotogram.core.resources.chatlist_story
import com.xbot.kotogram.core.resources.error_anerror
import com.xbot.kotogram.core.resources.navigation_edit
import com.xbot.kotogram.core.resources.searchfield_search
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.core.resources.touchbarlabel_newchat
import com.xbot.kotogram.feature.chats.impl.extensions.firstGrapheme
import io.xbot.tdlib.api.ChatListArchive
import io.xbot.tdlib.api.ChatListMain
import io.xbot.tdlib.api.FormattedText
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.orbitmvi.orbit.compose.collectAsState
import kotlin.time.Clock

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
    val currentOnAction by rememberUpdatedState(onAction)
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            info.totalItemsCount to (last >= info.totalItemsCount - END_THRESHOLD)
        }
            .distinctUntilChanged()
            .filter { (_, isNearEnd) -> isNearEnd }
            .collect { currentOnAction(ChatListScreenAction.OnEndReached) }
    }
    context(style) {
        SearchScaffold(
            title = { ChatListTitle(state.connection) },
            searchPlaceholder = stringResource(Res.string.searchfield_search),
            onSearchClick = { onAction(ChatListScreenAction.OnSearchClick) },
            listState = listState,
            modifier = modifier.background(KotogramTheme.colors.surface),
            editAction = BarTextAction(stringResource(Res.string.navigation_edit)) {
                onAction(ChatListScreenAction.OnEditClick)
            },
            actions = [
                BarAction(KotogramIcons.Camera, stringResource(Res.string.chatlist_story)) {
                    onAction(ChatListScreenAction.OnNewStoryClick)
                },
            ],
            primaryAction = BarAction(KotogramIcons.Compose, stringResource(Res.string.touchbarlabel_newchat)) {
                onAction(ChatListScreenAction.OnComposeClick)
            },
            header = {
                if (state.tabs.size > 1) {
                    FolderTabs(
                        tabs = state.tabs,
                        selected = state.selected,
                        customEmoji = state.customEmoji,
                        onSelect = { onAction(ChatListScreenAction.OnFolderSelect(it)) },
                    )
                }
            },
        ) { contentPadding ->
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
                val archive = state.archive
                if (archive != null && state.selected == ChatListMain) {
                    item(key = ARCHIVE_KEY, contentType = ARCHIVE_KEY) {
                        ArchiveRow(
                            summary = archive,
                            separator = ChatListItemSeparator.FullWidth,
                            onClick = { onAction(ChatListScreenAction.OnFolderSelect(ChatListArchive)) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
                itemsIndexed(
                    items = state.rows,
                    key = { _, row -> row.chatId },
                    contentType = { _, _ -> "chat" },
                ) { index, row ->
                    val next = state.rows.getOrNull(index + 1)
                    ChatRow(
                        row = row,
                        customEmoji = state.customEmoji,
                        separator = if (next == null || (row.isPinned && !next.isPinned)) {
                            ChatListItemSeparator.FullWidth
                        } else {
                            ChatListItemSeparator.Inset
                        },
                        onClick = { onChatClick(row.chatId) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
            if (state.rows.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
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

@PreviewKotogram
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

internal val SampleRows = [
    sampleRow(1, "Anastasia", "And you?", date = minutesAgo(10), unread = 5, color = 0),
    sampleRow(
        2,
        "I want to be with you",
        "Hi, it's Nastya. I'm about to turn thirty, I have a husband and a son…",
        date = minutesAgo(3 * DAY_MINUTES),
        muted = true,
        pinned = true,
        color = 2,
    ),
    sampleRow(
        3,
        "Nastya",
        "goodnight",
        date = minutesAgo(30 * DAY_MINUTES),
        pinned = true,
        color = 3,
        status = MessageStatus.Read,
    ),
    sampleRow(
        4,
        "Emergency | News 18+",
        "A ten-year-old boy crashed his quad bike",
        date = minutesAgo(120),
        unread = 537,
        muted = true,
        color = 0,
        photo = true,
    ),
    sampleRow(
        5,
        "Live • News",
        "",
        date = minutesAgo(121),
        unread = 37,
        muted = true,
        color = 3,
        photo = true,
    ),
    sampleRow(
        6,
        "Rozetked Discuss",
        "Sure, and some more memory for it…",
        date = minutesAgo(123),
        unread = 189_700,
        muted = true,
        color = 1,
        sender = "King Crimson",
    ),
    sampleRow(
        7,
        "Proxy MTProto",
        "Server: Unknown Port: 443 Secret: ee…",
        date = minutesAgo(125),
        unread = 6_300,
        muted = true,
        color = 5,
    ),
]

@Suppress("LongParameterList")
private fun sampleRow(
    id: Long,
    title: String,
    text: String,
    date: Int,
    color: Int,
    unread: Int = 0,
    muted: Boolean = false,
    pinned: Boolean = false,
    status: MessageStatus? = null,
    photo: Boolean = false,
    sender: String? = null,
) = ChatListRow(
    chatId = id,
    title = RowTitle.Name(title),
    avatar = AvatarModel(photo = null, thumbnail = null, letters = title.firstGrapheme(), colorIndex = color),
    stories = null,
    avatarBadge = null,
    mark = null,
    emojiStatusId = null,
    botVerificationEmojiId = null,
    isSecret = false,
    isDirectMessages = false,
    isMuted = muted,
    isPinned = pinned,
    date = date,
    status = status,
    unreadCount = unread,
    hasUnreadMention = false,
    hasUnreadReaction = false,
    hasUnreadPollVotes = false,
    isMarkedAsUnread = false,
    author = sender?.let(AuthorName::Name),
    preview = ChatPreview.Message(
        content = if (photo && text.isEmpty()) {
            PreviewText.Resource(StringResource.Plural(Res.plurals.chat_list_photo1, 1, 1))
        } else {
            PreviewText.Formatted(FormattedText(text = text))
        },
        icon = null,
        thumbnails = [],
        hideAuthor = false,
    ),
    typing = null,
    topics = [],
    tags = [],
    hasOpenAppButton = false,
)

private fun minutesAgo(minutes: Int): Int = (Clock.System.now().epochSeconds - minutes * MINUTE_SECONDS).toInt()

private const val MINUTE_SECONDS = 60L
private const val ARCHIVE_KEY = "archive"
private const val DAY_MINUTES = 24 * 60

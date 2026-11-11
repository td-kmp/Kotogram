package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.chatlist_filter_all
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.chats.domain.model.ChatFolderTab
import com.xbot.kotogram.feature.chats.impl.component.customEmojiContent
import com.xbot.kotogram.feature.chats.impl.component.toPreviewString
import com.xbot.kotogram.feature.chats.impl.extensions.customEmojiIds
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.Sticker

@Composable
context(style: ChatListScreenStyleScope)
internal fun FolderTabs(
    tabs: List<ChatFolderTab>,
    selected: ChatList,
    customEmoji: Map<Long, Sticker>,
    onSelect: (ChatList) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fill = tabs.size <= TABS_SHARING_WIDTH
    Row(
        modifier = modifier
            .padding(TabsPadding)
            .fillMaxWidth()
            .background(style.tabsColor, CircleShape)
            .padding(TabsInnerPadding)
            .then(if (fill) Modifier else Modifier.horizontalScroll(rememberScrollState())),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            Tab(
                tab = tab,
                isSelected = tab.chatList == selected,
                customEmoji = customEmoji,
                onClick = { onSelect(tab.chatList) },
                modifier = if (fill) Modifier.weight(1f) else Modifier,
            )
        }
    }
}

@Composable
context(style: ChatListScreenStyleScope)
private fun RowScope.Tab(
    tab: ChatFolderTab,
    isSelected: Boolean,
    customEmoji: Map<Long, Sticker>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(TabHeight)
            .background(if (isSelected) style.selectedTabColor else Color.Transparent, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = TabContentPadding),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val name = tab.folder?.name?.text
        val emojiSize = with(LocalDensity.current) { style.tabEmojiSize.toSp() }
        Text(
            text =
                name?.toPreviewString()
                    ?: androidx.compose.ui.text.AnnotatedString(
                        stringResource(Res.string.chatlist_filter_all),
                    ),
            style = style.tabTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            inlineContent = name?.let {
                customEmojiContent(customEmoji, it.customEmojiIds().toSet(), emojiSize)
            }.orEmpty(),
        )
        if (tab.unreadCount > 0) {
            Spacer(Modifier.width(TabBadgeGap))
            Box(
                modifier = Modifier
                    .height(TabBadgeHeight)
                    .defaultMinSize(minWidth = TabBadgeHeight)
                    .background(
                        if (tab.hasUnmutedUnread) style.tabBadgeColor else style.tabMutedBadgeColor,
                        CircleShape,
                    )
                    .padding(horizontal = style.tabBadgePadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(tab.unreadCount.toString(), style = style.tabBadgeTextStyle, maxLines = 1)
            }
        }
    }
}

private const val TABS_SHARING_WIDTH = 3

private val TabsPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
private val TabsInnerPadding = 4.dp
private val TabHeight = 36.dp
private val TabContentPadding = 16.dp
private val TabBadgeHeight = 18.dp
private val TabBadgeGap = 6.dp

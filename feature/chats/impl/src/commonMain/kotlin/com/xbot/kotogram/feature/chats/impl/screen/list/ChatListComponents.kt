package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.chats_draft
import com.xbot.kotogram.core.resources.chats_sender
import com.xbot.kotogram.core.resources.preview_other
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.chats.impl.extensions.label

@Composable
internal fun ChatRow(row: ChatListRow, onClick: () -> Unit, modifier: Modifier = Modifier) {
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

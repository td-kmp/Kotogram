package com.xbot.kotogram.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(onChatClick: (Long) -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Kotogram") }) }
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            items(sampleChats, key = { it.id }) { chat ->
                ChatListItem(chat = chat, onClick = { onChatClick(chat.id) })
            }
        }
    }
}

@Composable
private fun ChatListItem(chat: SampleChat, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { Avatar(chat.title) },
        headlineContent = { Text(chat.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Text(chat.lastMessage, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(chat.time, style = MaterialTheme.typography.labelSmall)
                if (chat.unreadCount > 0) {
                    Spacer(Modifier.padding(top = 4.dp))
                    Badge { Text(chat.unreadCount.toString()) }
                }
            }
        }
    )
}

@Composable
internal fun Avatar(title: String, modifier: Modifier = Modifier) {
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

@Preview
@Composable
private fun ChatListScreenPreview() {
    MaterialTheme {
        ChatListScreen(onChatClick = {})
    }
}

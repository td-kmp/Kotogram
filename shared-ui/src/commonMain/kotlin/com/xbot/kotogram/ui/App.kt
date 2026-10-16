package com.xbot.kotogram.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun App() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        var openedChat by rememberSaveable { mutableStateOf<Long?>(null) }
        when (val chatId = openedChat) {
            null -> ChatListScreen(onChatClick = { openedChat = it })
            else -> ChatScreen(chatId = chatId, onBack = { openedChat = null })
        }
    }
}

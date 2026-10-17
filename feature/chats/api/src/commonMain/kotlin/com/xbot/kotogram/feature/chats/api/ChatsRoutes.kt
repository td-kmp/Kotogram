package com.xbot.kotogram.feature.chats.api

import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.Navigator
import kotlinx.serialization.Serializable

/** The chat list, the main screen of an authorized account. */
@Serializable
data object ChatListRoute : NavKey

@Serializable
data class ChatRoute(val chatId: Long) : NavKey

fun Navigator.navigateToChat(chatId: Long) {
    navigate(ChatRoute(chatId))
}

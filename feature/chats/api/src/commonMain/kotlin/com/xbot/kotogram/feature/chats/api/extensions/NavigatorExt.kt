package com.xbot.kotogram.feature.chats.api.extensions

import com.xbot.kotogram.core.navigation.Navigator
import com.xbot.kotogram.feature.chats.api.ChatRoute

fun Navigator.navigateToChat(chatId: Long) {
    navigate(ChatRoute(chatId))
}

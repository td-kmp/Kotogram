package com.xbot.kotogram.feature.chats.api

import com.xbot.kotogram.core.navigation.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ChatListRoute : NavKey

@Serializable
data class ChatRoute(
    val chatId: Long,
) : NavKey

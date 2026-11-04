package com.xbot.kotogram.feature.chats.presentation.di

import androidx.lifecycle.compose.LocalLifecycleOwner
import com.xbot.kotogram.core.navigation.LocalNavigator
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.dropUnlessResumed
import com.xbot.kotogram.core.navigation.polymorphic
import com.xbot.kotogram.feature.chats.api.ChatListRoute
import com.xbot.kotogram.feature.chats.api.ChatRoute
import com.xbot.kotogram.feature.chats.api.navigateToChat
import com.xbot.kotogram.feature.chats.presentation.screen.chat.ChatScreen
import com.xbot.kotogram.feature.chats.presentation.screen.list.ChatListScreen
import kotlinx.serialization.modules.subclass
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val chatsFeatureModule = module {
    polymorphic<NavKey> {
        subclass(ChatListRoute::class)
        subclass(ChatRoute::class)
    }

    navigation<ChatListRoute> {
        val navigator = LocalNavigator.current
        val lifecycleOwner = LocalLifecycleOwner.current
        ChatListScreen(
            onChatClick = { chatId ->
                lifecycleOwner.dropUnlessResumed { navigator.navigateToChat(chatId) }.invoke()
            }
        )
    }

    navigation<ChatRoute> { route ->
        val navigator = LocalNavigator.current
        val lifecycleOwner = LocalLifecycleOwner.current
        ChatScreen(
            chatId = route.chatId,
            onBackClick = lifecycleOwner.dropUnlessResumed { navigator.navigateBack() }
        )
    }
}

package com.xbot.kotogram.feature.chats.impl.navigation

import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import com.xbot.kotogram.core.navigation.FeatureNavigation
import com.xbot.kotogram.core.navigation.LocalNavigator
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.extensions.dropUnlessResumed
import com.xbot.kotogram.feature.chats.api.ChatListRoute
import com.xbot.kotogram.feature.chats.api.ChatRoute
import com.xbot.kotogram.feature.chats.api.extensions.navigateToChat
import com.xbot.kotogram.feature.chats.impl.screen.chat.ChatScreen
import com.xbot.kotogram.feature.chats.impl.screen.list.ChatListScreen
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

object ChatsNavigation : FeatureNavigation {
    override fun PolymorphicModuleBuilder<NavKey>.routes() {
        subclass(ChatListRoute::class)
        subclass(ChatRoute::class)
    }

    override fun EntryProviderScope<NavKey>.entries() {
        entry<ChatListRoute> {
            val navigator = LocalNavigator.current
            val lifecycleOwner = LocalLifecycleOwner.current
            ChatListScreen(
                viewModel = koinViewModel(),
                onChatClick = { chatId ->
                    lifecycleOwner.dropUnlessResumed { navigator.navigateToChat(chatId) }.invoke()
                }
            )
        }

        entry<ChatRoute> { route ->
            val navigator = LocalNavigator.current
            val lifecycleOwner = LocalLifecycleOwner.current
            ChatScreen(
                viewModel = koinViewModel { parametersOf(route.chatId) },
                onBackClick = lifecycleOwner.dropUnlessResumed { navigator.navigateBack() }
            )
        }
    }
}

package com.xbot.kotogram.feature.chats.impl.di

import androidx.lifecycle.compose.LocalLifecycleOwner
import com.xbot.kotogram.core.account.AccountScope
import com.xbot.kotogram.core.navigation.LocalNavigator
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.extensions.dropUnlessResumed
import com.xbot.kotogram.core.navigation.polymorphic
import com.xbot.kotogram.feature.chats.api.ChatListRoute
import com.xbot.kotogram.feature.chats.api.ChatRoute
import com.xbot.kotogram.feature.chats.api.extensions.navigateToChat
import com.xbot.kotogram.feature.chats.impl.screen.chat.ChatScreen
import com.xbot.kotogram.feature.chats.impl.screen.chat.ChatViewModel
import com.xbot.kotogram.feature.chats.impl.screen.list.ChatListScreen
import com.xbot.kotogram.feature.chats.impl.screen.list.ChatListViewModel
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val chatsImplModule = module {
    polymorphic<NavKey> {
        subclass(ChatListRoute::class)
        subclass(ChatRoute::class)
    }

    navigation<ChatListRoute> {
        val navigator = LocalNavigator.current
        val lifecycleOwner = LocalLifecycleOwner.current
        ChatListScreen(
            viewModel = koinViewModel(),
            onChatClick = { chatId ->
                lifecycleOwner.dropUnlessResumed { navigator.navigateToChat(chatId) }.invoke()
            }
        )
    }

    navigation<ChatRoute> { route ->
        val navigator = LocalNavigator.current
        val lifecycleOwner = LocalLifecycleOwner.current
        ChatScreen(
            viewModel = koinViewModel { parametersOf(route.chatId) },
            onBackClick = lifecycleOwner.dropUnlessResumed { navigator.navigateBack() }
        )
    }

    scope(AccountScope) {
        viewModelOf(::ChatListViewModel)
        viewModel { parameters -> ChatViewModel(parameters.get(), get()) }
    }
}

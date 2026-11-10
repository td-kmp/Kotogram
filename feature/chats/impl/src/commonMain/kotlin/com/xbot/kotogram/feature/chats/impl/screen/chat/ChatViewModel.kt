package com.xbot.kotogram.feature.chats.impl.screen.chat

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.chats.domain.repository.ChatRepository
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class ChatViewModel(
    chatId: Long,
    repository: ChatRepository,
) : ViewModel(),
    OrbitContainerHost<ChatScreenState, ChatScreenState, Nothing> {

    override val container = orbitContainer<ChatScreenState, Nothing>(ChatScreenState()) {
        repeatOnSubscription {
            repository.chat(chatId).collect { chat ->
                reduce { state.copy(title = chat?.title.orEmpty()) }
            }
        }
    }
}

package com.xbot.kotogram.feature.chats.presentation.screen.chat

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.feature.chats.domain.repository.ChatRepository
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Scope
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal data class ChatScreenState(val title: String = "")

/** Only the header for now: the history is next (docs/design/messaging-core.md §6). */
@Scope(AccountId::class)
@KoinViewModel
internal class ChatViewModel(@InjectedParam chatId: Long, repository: ChatRepository) :
    ViewModel(),
    OrbitContainerHost<ChatScreenState, ChatScreenState, Nothing> {

    override val container = orbitContainer<ChatScreenState, Nothing>(ChatScreenState()) {
        repeatOnSubscription {
            repository.chat(chatId).collect { chat ->
                reduce { state.copy(title = chat?.title.orEmpty()) }
            }
        }
    }
}

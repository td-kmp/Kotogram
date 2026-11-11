package com.xbot.kotogram.feature.chats.data.store.reduce

import com.xbot.kotogram.feature.chats.data.store.ChatsState
import io.xbot.tdlib.api.OptionValueInteger
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateOption
import io.xbot.tdlib.api.UpdateSecretChat
import io.xbot.tdlib.api.UpdateSupergroup

internal object PeerReducer : Reducer {
    override fun reduce(
        state: ChatsState,
        update: Update,
    ): ChatsState = when (update) {
        is UpdateSupergroup -> state.copy(
            supergroups = state.supergroups.putting(update.supergroup.id, update.supergroup),
        )

        is UpdateSecretChat -> state.copy(
            secretChats = state.secretChats.putting(update.secretChat.id, update.secretChat),
        )

        is UpdateOption -> state.reduceOption(update)

        else -> state
    }

    private fun ChatsState.reduceOption(update: UpdateOption): ChatsState {
        val value = (update.value as? OptionValueInteger)?.value ?: 0L
        return when (update.name) {
            "my_id" -> copy(options = options.copy(myUserId = value))
            "replies_bot_chat_id" -> copy(options = options.copy(repliesChatId = value))
            "telegram_service_notifications_chat_id" -> copy(options = options.copy(serviceChatId = value))
            "verification_codes_bot_chat_id" -> copy(options = options.copy(verificationCodesChatId = value))
            else -> this
        }
    }
}

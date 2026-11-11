package com.xbot.kotogram.feature.chats.data.store.reduce

import com.xbot.kotogram.feature.chats.data.store.ChatActionEntry
import com.xbot.kotogram.feature.chats.data.store.ChatsState
import com.xbot.kotogram.feature.chats.data.store.NotificationScope
import io.xbot.tdlib.api.ChatActionCancel
import io.xbot.tdlib.api.ChatActiveStories
import io.xbot.tdlib.api.NotificationSettingsScopeChannelChats
import io.xbot.tdlib.api.NotificationSettingsScopeGroupChats
import io.xbot.tdlib.api.NotificationSettingsScopePrivateChats
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateChatAction
import io.xbot.tdlib.api.UpdateChatActiveStories
import io.xbot.tdlib.api.UpdateScopeNotificationSettings
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf

internal object ActivityReducer : Reducer {
    override fun reduce(
        state: ChatsState,
        update: Update,
    ): ChatsState = when (update) {
        is UpdateChatAction -> {
            val current = state.actions[update.chatId] ?: persistentListOf()
            val others = current.removingAll { it.sender == update.senderId }
            val next = if (update.action is ChatActionCancel) others else others.adding(
                ChatActionEntry(update.senderId, update.action),
            )
            state.copy(
                actions = if (next.isEmpty()) state.actions.removing(
                    update.chatId,
                ) else state.actions.putting(update.chatId, next),
            )
        }

        is UpdateChatActiveStories -> state.copy(stories = state.stories.withStories(update.activeStories))

        is UpdateScopeNotificationSettings -> {
            val scope = when (update.scope) {
                NotificationSettingsScopePrivateChats -> NotificationScope.PrivateChats
                NotificationSettingsScopeGroupChats -> NotificationScope.GroupChats
                NotificationSettingsScopeChannelChats -> NotificationScope.ChannelChats
            }
            state.copy(scopeSettings = state.scopeSettings.putting(scope, update.notificationSettings))
        }

        else -> state
    }

    private fun PersistentMap<Long, ChatActiveStories>.withStories(stories: ChatActiveStories) =
        if (stories.stories.isEmpty()) removing(stories.chatId) else putting(stories.chatId, stories)
}

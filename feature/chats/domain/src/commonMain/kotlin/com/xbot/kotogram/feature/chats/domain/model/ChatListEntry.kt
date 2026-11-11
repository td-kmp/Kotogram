package com.xbot.kotogram.feature.chats.domain.model

import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatAction
import io.xbot.tdlib.api.ChatActiveStories
import io.xbot.tdlib.api.ChatFolderInfo
import io.xbot.tdlib.api.ChatPosition
import io.xbot.tdlib.api.ForumTopicInfo
import io.xbot.tdlib.api.ScopeNotificationSettings
import io.xbot.tdlib.api.SecretChat
import io.xbot.tdlib.api.Supergroup
import io.xbot.tdlib.api.User

class ChatListEntry(
    val chat: Chat,
    val position: ChatPosition?,
    val senderUser: User?,
    val senderChat: Chat?,
    val user: User? = null,
    val supergroup: Supergroup? = null,
    val secretChat: SecretChat? = null,
    val typing: List<ChatTyping> = emptyList(),
    val stories: ChatActiveStories? = null,
    val scopeSettings: ScopeNotificationSettings? = null,
    val topics: List<ForumTopicEntry> = emptyList(),
    val tags: List<ChatFolderInfo> = emptyList(),
    val mentionedUsers: List<User> = emptyList(),
    val special: SpecialChat? = null,
    val myUserId: Long = 0L,
)

class ChatTyping(
    val user: User?,
    val chat: Chat?,
    val action: ChatAction,
)

class ForumTopicEntry(
    val info: ForumTopicInfo,
    val isUnread: Boolean,
)

enum class SpecialChat { SavedMessages, Replies, ServiceNotifications, VerificationCodes }

class ArchiveSummary(
    val chats: List<Chat>,
    val unreadCount: Int,
    val hasUnmutedUnread: Boolean,
)

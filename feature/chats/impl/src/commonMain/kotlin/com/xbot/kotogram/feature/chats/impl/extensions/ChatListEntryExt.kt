package com.xbot.kotogram.feature.chats.impl.extensions

import com.xbot.kotogram.feature.chats.domain.model.ChatListEntry
import com.xbot.kotogram.feature.chats.impl.screen.list.AvatarModel
import com.xbot.kotogram.feature.chats.impl.screen.list.ChatListRow
import com.xbot.kotogram.feature.chats.impl.screen.list.ChatPreview
import com.xbot.kotogram.feature.chats.impl.screen.list.OutgoingStatus
import io.xbot.tdlib.api.ChatTypeBasicGroup
import io.xbot.tdlib.api.ChatTypeSupergroup
import io.xbot.tdlib.api.EmojiStatusTypeCustomEmoji
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

internal fun ChatListEntry.toRow(): ChatListRow {
    val message = chat.lastMessage
    val draft = chat.draftMessage?.toPreview()
    return ChatListRow(
        chatId = chat.id,
        title = chat.title,
        avatar = AvatarModel(
            photo = chat.photo?.small,
            thumbnail = chat.photo?.minithumbnail?.data,
            letters = chat.title.letters(),
            colorIndex = chat.accentColorId
        ),
        emojiStatusId = (chat.emojiStatus?.type as? EmojiStatusTypeCustomEmoji)?.customEmojiId,
        // A mute by the scope's default isn't resolved: the store has no scope settings yet.
        isMuted =
            !chat.notificationSettings.useDefaultMuteFor && chat.notificationSettings.muteFor > 0,
        isPinned = position?.isPinned == true,
        time = message?.date?.let(::formatDate).orEmpty(),
        outgoing = message?.takeIf { it.isOutgoing && draft == null }?.let {
            if (it.id <= chat.lastReadOutboxMessageId) OutgoingStatus.Read else OutgoingStatus.Sent
        },
        unreadCount = chat.unreadCount,
        hasUnreadMention = chat.unreadMentionCount > 0,
        isMarkedAsUnread = chat.isMarkedAsUnread,
        senderName = senderName().takeIf { draft == null },
        preview = draft ?: message?.content?.toPreview() ?: ChatPreview.Empty
    )
}

// Telegram's displayLetters: the first letters of the first two words.
private fun String.letters(): String =
    split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }

private fun ChatListEntry.senderName(): String? {
    val isGroup =
        chat.type is ChatTypeBasicGroup || (chat.type as? ChatTypeSupergroup)?.isChannel == false
    if (!isGroup) return null
    return senderUser?.firstName ?: senderChat?.title
}

private fun formatDate(unixSeconds: Int): String {
    val zone = TimeZone.currentSystemDefault()
    val date = Instant.fromEpochSeconds(unixSeconds.toLong()).toLocalDateTime(zone)
    val today = Clock.System.now().toLocalDateTime(zone).date
    return if (date.date == today) {
        "${date.hour.pad()}:${date.minute.pad()}"
    } else {
        "${date.day.pad()}.${date.month.number.pad()}"
    }
}

internal fun ChatListEntry.hasSameInputsAs(other: ChatListEntry) = chat === other.chat &&
    position === other.position &&
    senderUser === other.senderUser &&
    senderChat === other.senderChat

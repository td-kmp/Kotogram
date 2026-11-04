package com.xbot.kotogram.feature.chats.impl.extensions

import com.xbot.kotogram.feature.chats.domain.model.ChatListEntry
import com.xbot.kotogram.feature.chats.impl.screen.list.ChatListRow
import com.xbot.kotogram.feature.chats.impl.screen.list.ChatPreview
import io.xbot.tdlib.api.ChatTypeBasicGroup
import io.xbot.tdlib.api.ChatTypeSupergroup
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

internal fun ChatListEntry.toRow(): ChatListRow {
    val message = chat.lastMessage
    val draft = chat.draftMessage
    return ChatListRow(
        chatId = chat.id,
        title = chat.title,
        preview = draft?.toPreview() ?: message?.content?.toPreview() ?: ChatPreview.Empty,
        senderName = senderName().takeIf { draft == null },
        time = message?.date?.let(::formatDate).orEmpty(),
        unreadCount = chat.unreadCount,
        // A mute by the scope's default isn't resolved: the store has no scope settings yet.
        isMuted =
            !chat.notificationSettings.useDefaultMuteFor && chat.notificationSettings.muteFor > 0,
        isPinned = position?.isPinned == true
    )
}

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

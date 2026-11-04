package com.xbot.kotogram.feature.chats.presentation.screen.list

import com.xbot.kotogram.feature.chats.domain.model.ChatListEntry
import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatPosition
import io.xbot.tdlib.api.ChatTypeBasicGroup
import io.xbot.tdlib.api.ChatTypeSupergroup
import io.xbot.tdlib.api.DraftMessage
import io.xbot.tdlib.api.DraftMessageContentText
import io.xbot.tdlib.api.MessageAnimation
import io.xbot.tdlib.api.MessageAudio
import io.xbot.tdlib.api.MessageContent
import io.xbot.tdlib.api.MessageDocument
import io.xbot.tdlib.api.MessagePhoto
import io.xbot.tdlib.api.MessageSticker
import io.xbot.tdlib.api.MessageText
import io.xbot.tdlib.api.MessageVideo
import io.xbot.tdlib.api.MessageVideoNote
import io.xbot.tdlib.api.MessageVoiceNote
import io.xbot.tdlib.api.User
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

/** Reuses a row while its inputs are the same instances. Not thread-safe: one collector at a time. */
internal class ChatListRowMapper {
    private var memo = emptyMap<Long, Memo>()

    fun map(entries: List<ChatListEntry>): List<ChatListRow> {
        val next = HashMap<Long, Memo>(entries.size)
        val rows = entries.map { entry ->
            val memoized = memo[entry.chat.id]?.takeIf { it.isFor(entry) } ?: Memo(entry)
            next[entry.chat.id] = memoized
            memoized.row
        }
        // Dropping what left the list also forgets a previous login: its snapshot was emptied.
        memo = next
        return rows
    }

    private class Memo(entry: ChatListEntry) {
        private val chat: Chat = entry.chat
        private val position: ChatPosition? = entry.position
        private val senderUser: User? = entry.senderUser
        private val senderChat: Chat? = entry.senderChat
        val row: ChatListRow = entry.toRow()

        fun isFor(entry: ChatListEntry) = chat === entry.chat &&
            position === entry.position &&
            senderUser === entry.senderUser &&
            senderChat === entry.senderChat
    }
}

private fun ChatListEntry.toRow(): ChatListRow {
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

private fun DraftMessage.toPreview(): ChatPreview? =
    (content as? DraftMessageContentText)?.let { ChatPreview.Draft(it.text.text) }

private fun MessageContent.toPreview(): ChatPreview = when (this) {
    is MessageText -> ChatPreview.Text(text.text)
    is MessagePhoto -> ChatPreview.Media(MediaKind.Photo, caption.text)
    is MessageVideo -> ChatPreview.Media(MediaKind.Video, caption.text)
    is MessageDocument -> ChatPreview.Media(MediaKind.Document, caption.text)
    is MessageAudio -> ChatPreview.Media(MediaKind.Audio, caption.text)
    is MessageVoiceNote -> ChatPreview.Media(MediaKind.VoiceNote, caption.text)
    is MessageAnimation -> ChatPreview.Media(MediaKind.Animation, caption.text)
    is MessageVideoNote -> ChatPreview.Media(MediaKind.VideoNote, "")
    is MessageSticker -> ChatPreview.Media(MediaKind.Sticker, sticker.emoji)
    else -> ChatPreview.Other
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

private fun Int.pad() = toString().padStart(2, '0')

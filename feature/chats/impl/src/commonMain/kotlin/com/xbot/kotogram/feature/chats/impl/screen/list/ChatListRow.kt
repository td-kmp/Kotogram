package com.xbot.kotogram.feature.chats.impl.screen.list

import com.xbot.kotogram.feature.chats.impl.extensions.customEmojiIds
import io.xbot.tdlib.api.File
import io.xbot.tdlib.api.FormattedText

// A plain class: the mapper keeps the instance while the inputs are the same, so strong skipping's === skips it.
internal class ChatListRow(
    val chatId: Long,
    val title: String,
    val avatar: AvatarModel,
    val emojiStatusId: Long?,
    val isMuted: Boolean,
    val isPinned: Boolean,
    val time: String,
    val outgoing: OutgoingStatus?,
    val unreadCount: Int,
    val hasUnreadMention: Boolean,
    val isMarkedAsUnread: Boolean,
    val senderName: String?,
    val preview: ChatPreview
) {
    val customEmojiIds: Set<Long> = buildSet {
        emojiStatusId?.let(::add)
        preview.text?.customEmojiIds()?.let(::addAll)
    }
}

internal class AvatarModel(
    val photo: File?,
    val thumbnail: ByteArray?,
    val letters: String,
    val colorIndex: Int
)

internal enum class OutgoingStatus { Sent, Read }

internal sealed interface ChatPreview {
    val text: FormattedText? get() = null

    data object Empty : ChatPreview

    class Text(override val text: FormattedText) : ChatPreview

    class Draft(override val text: FormattedText) : ChatPreview

    // The thumbnail is a minithumbnail's JPEG or a TDLib file.
    class Media(val kind: MediaKind, override val text: FormattedText, val thumbnail: Any?) :
        ChatPreview

    data object Other : ChatPreview
}

internal enum class MediaKind {
    Photo,
    Video,
    Document,
    Audio,
    VoiceNote,
    VideoNote,
    Animation,
    Sticker
}

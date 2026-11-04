package com.xbot.kotogram.feature.chats.impl.screen.list

/**
 * Display values of one row, previewable and free of DTOs. A plain class: the mapper keeps the
 * instance while the row's inputs are the same, so strong skipping's `===` skips unchanged rows.
 */
internal class ChatListRow(
    val chatId: Long,
    val title: String,
    val preview: ChatPreview,
    val senderName: String?,
    val time: String,
    val unreadCount: Int,
    val isMuted: Boolean,
    val isPinned: Boolean
)

internal sealed interface ChatPreview {
    data object Empty : ChatPreview

    data class Text(val text: String) : ChatPreview

    data class Draft(val text: String) : ChatPreview

    data class Media(val kind: MediaKind, val caption: String) : ChatPreview

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

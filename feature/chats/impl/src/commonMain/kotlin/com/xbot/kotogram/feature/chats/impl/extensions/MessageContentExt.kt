package com.xbot.kotogram.feature.chats.impl.extensions

import com.xbot.kotogram.feature.chats.impl.screen.list.ChatPreview
import com.xbot.kotogram.feature.chats.impl.screen.list.MediaKind
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

internal fun MessageContent.toPreview(): ChatPreview = when (this) {
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

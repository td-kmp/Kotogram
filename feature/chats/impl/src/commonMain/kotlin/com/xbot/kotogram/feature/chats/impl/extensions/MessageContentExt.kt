package com.xbot.kotogram.feature.chats.impl.extensions

import com.xbot.kotogram.feature.chats.impl.screen.list.ChatPreview
import com.xbot.kotogram.feature.chats.impl.screen.list.MediaKind
import io.xbot.tdlib.api.FormattedText
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
    is MessageText -> ChatPreview.Text(text)

    is MessagePhoto -> ChatPreview.Media(MediaKind.Photo, caption, photo.minithumbnail?.data)

    is MessageVideo -> ChatPreview.Media(MediaKind.Video, caption, video.minithumbnail?.data)

    is MessageDocument -> ChatPreview.Media(
        MediaKind.Document,
        caption,
        document.minithumbnail?.data
    )

    is MessageAudio -> ChatPreview.Media(MediaKind.Audio, caption, null)

    is MessageVoiceNote -> ChatPreview.Media(MediaKind.VoiceNote, caption, null)

    is MessageAnimation -> ChatPreview.Media(
        MediaKind.Animation,
        caption,
        animation.minithumbnail?.data
    )

    is MessageVideoNote -> ChatPreview.Media(
        MediaKind.VideoNote,
        FormattedText(),
        videoNote.minithumbnail?.data
    )

    is MessageSticker -> ChatPreview.Media(
        MediaKind.Sticker,
        FormattedText(text = sticker.emoji),
        sticker.thumbnail?.file
    )

    else -> ChatPreview.Other
}

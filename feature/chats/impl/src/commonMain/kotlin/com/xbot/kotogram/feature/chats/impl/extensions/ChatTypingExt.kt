package com.xbot.kotogram.feature.chats.impl.extensions

import com.xbot.kotogram.feature.chats.domain.model.ChatTyping
import com.xbot.kotogram.feature.chats.impl.screen.list.TypingKind
import io.xbot.tdlib.api.ChatActionChoosingContact
import io.xbot.tdlib.api.ChatActionChoosingLocation
import io.xbot.tdlib.api.ChatActionChoosingSticker
import io.xbot.tdlib.api.ChatActionRecordingVideo
import io.xbot.tdlib.api.ChatActionRecordingVideoNote
import io.xbot.tdlib.api.ChatActionRecordingVoiceNote
import io.xbot.tdlib.api.ChatActionStartPlayingGame
import io.xbot.tdlib.api.ChatActionTyping
import io.xbot.tdlib.api.ChatActionUploadingDocument
import io.xbot.tdlib.api.ChatActionUploadingPhoto
import io.xbot.tdlib.api.ChatActionUploadingVideo
import io.xbot.tdlib.api.ChatActionUploadingVideoNote
import io.xbot.tdlib.api.ChatActionUploadingVoiceNote

internal fun ChatTyping.kind(): TypingKind? = when (action) {
    ChatActionTyping, ChatActionChoosingLocation, ChatActionChoosingContact -> TypingKind.Text
    ChatActionRecordingVoiceNote -> TypingKind.Voice
    ChatActionRecordingVideo, ChatActionRecordingVideoNote -> TypingKind.Video
    is ChatActionUploadingPhoto -> TypingKind.Photo
    is ChatActionUploadingVideo, is ChatActionUploadingVideoNote -> TypingKind.VideoUpload
    is ChatActionUploadingDocument, is ChatActionUploadingVoiceNote -> TypingKind.File
    ChatActionStartPlayingGame -> TypingKind.Game
    ChatActionChoosingSticker -> TypingKind.Sticker
    else -> null
}

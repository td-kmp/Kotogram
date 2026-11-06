package com.xbot.kotogram.feature.chats.impl.extensions

import androidx.compose.runtime.getValue
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.preview_animation
import com.xbot.kotogram.core.resources.preview_audio
import com.xbot.kotogram.core.resources.preview_document
import com.xbot.kotogram.core.resources.preview_photo
import com.xbot.kotogram.core.resources.preview_sticker
import com.xbot.kotogram.core.resources.preview_video
import com.xbot.kotogram.core.resources.preview_video_note
import com.xbot.kotogram.core.resources.preview_voice_note
import com.xbot.kotogram.feature.chats.impl.screen.list.MediaKind
import org.jetbrains.compose.resources.StringResource

internal fun MediaKind.label(): StringResource = when (this) {
    MediaKind.Photo -> Res.string.preview_photo
    MediaKind.Video -> Res.string.preview_video
    MediaKind.Document -> Res.string.preview_document
    MediaKind.Audio -> Res.string.preview_audio
    MediaKind.VoiceNote -> Res.string.preview_voice_note
    MediaKind.VideoNote -> Res.string.preview_video_note
    MediaKind.Animation -> Res.string.preview_animation
    MediaKind.Sticker -> Res.string.preview_sticker
}

package com.xbot.kotogram.feature.chats.impl.extensions

import androidx.compose.runtime.getValue
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.chat_list_gif
import com.xbot.kotogram.core.resources.chat_list_instantvideo
import com.xbot.kotogram.core.resources.chat_list_video
import com.xbot.kotogram.core.resources.chat_list_voice
import com.xbot.kotogram.core.resources.inputattach_audio
import com.xbot.kotogram.core.resources.inputattach_popover_file
import com.xbot.kotogram.core.resources.inputattach_popover_photo
import com.xbot.kotogram.core.resources.newpoll_attachsticker
import com.xbot.kotogram.feature.chats.impl.screen.list.MediaKind
import org.jetbrains.compose.resources.StringResource

internal fun MediaKind.label(): StringResource = when (this) {
    MediaKind.Photo -> Res.string.inputattach_popover_photo
    MediaKind.Video -> Res.string.chat_list_video
    MediaKind.Document -> Res.string.inputattach_popover_file
    MediaKind.Audio -> Res.string.inputattach_audio
    MediaKind.VoiceNote -> Res.string.chat_list_voice
    MediaKind.VideoNote -> Res.string.chat_list_instantvideo
    MediaKind.Animation -> Res.string.chat_list_gif
    MediaKind.Sticker -> Res.string.newpoll_attachsticker
}

package com.xbot.kotogram.feature.chats.impl.extensions

import com.xbot.kotogram.core.designsystem.component.TypingAction
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.peer_activity_chat_choosingsticker
import com.xbot.kotogram.core.resources.peer_activity_chat_multi_choosingsticker1
import com.xbot.kotogram.core.resources.peer_activity_chat_multi_playinggame1
import com.xbot.kotogram.core.resources.peer_activity_chat_multi_recordingaudio1
import com.xbot.kotogram.core.resources.peer_activity_chat_multi_recordingvideo1
import com.xbot.kotogram.core.resources.peer_activity_chat_multi_sendingfile1
import com.xbot.kotogram.core.resources.peer_activity_chat_multi_sendingphoto1
import com.xbot.kotogram.core.resources.peer_activity_chat_multi_sendingvideo1
import com.xbot.kotogram.core.resources.peer_activity_chat_multi_typingtext1
import com.xbot.kotogram.core.resources.peer_activity_chat_playinggame
import com.xbot.kotogram.core.resources.peer_activity_chat_recordingaudio
import com.xbot.kotogram.core.resources.peer_activity_chat_recordingvideo
import com.xbot.kotogram.core.resources.peer_activity_chat_sendingfile
import com.xbot.kotogram.core.resources.peer_activity_chat_sendingphoto
import com.xbot.kotogram.core.resources.peer_activity_chat_sendingvideo
import com.xbot.kotogram.core.resources.peer_activity_chat_typingtext
import com.xbot.kotogram.core.resources.peer_activity_user_choosingsticker
import com.xbot.kotogram.core.resources.peer_activity_user_playinggame
import com.xbot.kotogram.core.resources.peer_activity_user_recordingaudio
import com.xbot.kotogram.core.resources.peer_activity_user_recordingvideo
import com.xbot.kotogram.core.resources.peer_activity_user_sendingfile
import com.xbot.kotogram.core.resources.peer_activity_user_sendingphoto
import com.xbot.kotogram.core.resources.peer_activity_user_sendingvideo
import com.xbot.kotogram.core.resources.peer_activity_user_typingtext
import com.xbot.kotogram.feature.chats.impl.screen.list.TypingKind
import org.jetbrains.compose.resources.StringResource as ComposeStringResource

internal fun TypingKind.action(): TypingAction = when (this) {
    TypingKind.Text -> TypingAction.Typing
    TypingKind.Voice -> TypingAction.RecordingVoice
    TypingKind.Video -> TypingAction.RecordingVideo
    TypingKind.Photo, TypingKind.VideoUpload, TypingKind.File -> TypingAction.Uploading
    TypingKind.Game -> TypingAction.PlayingGame
    TypingKind.Sticker -> TypingAction.ChoosingSticker
}

internal fun TypingKind.userText(): ComposeStringResource = when (this) {
    TypingKind.Text -> Res.string.peer_activity_user_typingtext
    TypingKind.Voice -> Res.string.peer_activity_user_recordingaudio
    TypingKind.Video -> Res.string.peer_activity_user_recordingvideo
    TypingKind.Photo -> Res.string.peer_activity_user_sendingphoto
    TypingKind.VideoUpload -> Res.string.peer_activity_user_sendingvideo
    TypingKind.File -> Res.string.peer_activity_user_sendingfile
    TypingKind.Game -> Res.string.peer_activity_user_playinggame
    TypingKind.Sticker -> Res.string.peer_activity_user_choosingsticker
}

internal fun TypingKind.chatText(): ComposeStringResource = when (this) {
    TypingKind.Text -> Res.string.peer_activity_chat_typingtext
    TypingKind.Voice -> Res.string.peer_activity_chat_recordingaudio
    TypingKind.Video -> Res.string.peer_activity_chat_recordingvideo
    TypingKind.Photo -> Res.string.peer_activity_chat_sendingphoto
    TypingKind.VideoUpload -> Res.string.peer_activity_chat_sendingvideo
    TypingKind.File -> Res.string.peer_activity_chat_sendingfile
    TypingKind.Game -> Res.string.peer_activity_chat_playinggame
    TypingKind.Sticker -> Res.string.peer_activity_chat_choosingsticker
}

internal fun TypingKind.multiText(): ComposeStringResource = when (this) {
    TypingKind.Text -> Res.string.peer_activity_chat_multi_typingtext1
    TypingKind.Voice -> Res.string.peer_activity_chat_multi_recordingaudio1
    TypingKind.Video -> Res.string.peer_activity_chat_multi_recordingvideo1
    TypingKind.Photo -> Res.string.peer_activity_chat_multi_sendingphoto1
    TypingKind.VideoUpload -> Res.string.peer_activity_chat_multi_sendingvideo1
    TypingKind.File -> Res.string.peer_activity_chat_multi_sendingfile1
    TypingKind.Game -> Res.string.peer_activity_chat_multi_playinggame1
    TypingKind.Sticker -> Res.string.peer_activity_chat_multi_choosingsticker1
}

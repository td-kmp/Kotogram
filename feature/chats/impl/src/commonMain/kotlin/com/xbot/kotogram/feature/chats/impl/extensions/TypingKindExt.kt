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
    Text -> TypingAction.Typing
    Voice -> TypingAction.RecordingVoice
    Video -> TypingAction.RecordingVideo
    Photo, VideoUpload, File -> TypingAction.Uploading
    Game -> TypingAction.PlayingGame
    Sticker -> TypingAction.ChoosingSticker
}

internal fun TypingKind.userText(): ComposeStringResource = when (this) {
    Text -> Res.string.peer_activity_user_typingtext
    Voice -> Res.string.peer_activity_user_recordingaudio
    Video -> Res.string.peer_activity_user_recordingvideo
    Photo -> Res.string.peer_activity_user_sendingphoto
    VideoUpload -> Res.string.peer_activity_user_sendingvideo
    File -> Res.string.peer_activity_user_sendingfile
    Game -> Res.string.peer_activity_user_playinggame
    Sticker -> Res.string.peer_activity_user_choosingsticker
}

internal fun TypingKind.chatText(): ComposeStringResource = when (this) {
    Text -> Res.string.peer_activity_chat_typingtext
    Voice -> Res.string.peer_activity_chat_recordingaudio
    Video -> Res.string.peer_activity_chat_recordingvideo
    Photo -> Res.string.peer_activity_chat_sendingphoto
    VideoUpload -> Res.string.peer_activity_chat_sendingvideo
    File -> Res.string.peer_activity_chat_sendingfile
    Game -> Res.string.peer_activity_chat_playinggame
    Sticker -> Res.string.peer_activity_chat_choosingsticker
}

internal fun TypingKind.multiText(): ComposeStringResource = when (this) {
    Text -> Res.string.peer_activity_chat_multi_typingtext1
    Voice -> Res.string.peer_activity_chat_multi_recordingaudio1
    Video -> Res.string.peer_activity_chat_multi_recordingvideo1
    Photo -> Res.string.peer_activity_chat_multi_sendingphoto1
    VideoUpload -> Res.string.peer_activity_chat_multi_sendingvideo1
    File -> Res.string.peer_activity_chat_multi_sendingfile1
    Game -> Res.string.peer_activity_chat_multi_playinggame1
    Sticker -> Res.string.peer_activity_chat_multi_choosingsticker1
}

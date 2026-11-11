package com.xbot.kotogram.feature.chats.impl.extensions

import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.StringResource
import com.xbot.kotogram.core.resources.chat_list_instantvideo
import com.xbot.kotogram.core.resources.chat_list_voice
import com.xbot.kotogram.feature.chats.impl.screen.list.ChatPreview
import io.xbot.tdlib.api.DraftMessage
import io.xbot.tdlib.api.DraftMessageContentText
import io.xbot.tdlib.api.DraftMessageContentVideoNote
import io.xbot.tdlib.api.DraftMessageContentVoiceNote

internal fun DraftMessage.toPreview(): ChatPreview? = when (val content = content) {
    is DraftMessageContentText -> ChatPreview.Draft(content.text)
    is DraftMessageContentVoiceNote -> ChatPreview.MediaDraft(StringResource.Text(Res.string.chat_list_voice))
    is DraftMessageContentVideoNote -> ChatPreview.MediaDraft(StringResource.Text(Res.string.chat_list_instantvideo))
    else -> null
}

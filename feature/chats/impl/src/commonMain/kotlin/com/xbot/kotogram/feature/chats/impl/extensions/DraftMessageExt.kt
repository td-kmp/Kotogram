package com.xbot.kotogram.feature.chats.impl.extensions

import com.xbot.kotogram.feature.chats.impl.screen.list.ChatPreview
import io.xbot.tdlib.api.DraftMessage
import io.xbot.tdlib.api.DraftMessageContentText

internal fun DraftMessage.toPreview(): ChatPreview? =
    (content as? DraftMessageContentText)?.let { ChatPreview.Draft(it.text) }

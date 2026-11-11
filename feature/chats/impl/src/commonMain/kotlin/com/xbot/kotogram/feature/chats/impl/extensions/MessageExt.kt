package com.xbot.kotogram.feature.chats.impl.extensions

import com.xbot.kotogram.core.designsystem.component.MessageStatus
import io.xbot.tdlib.api.Message
import io.xbot.tdlib.api.MessageSendingStateFailed
import io.xbot.tdlib.api.MessageSendingStatePending

internal fun Message.status(
    lastReadOutboxMessageId: Long,
    savedMessages: Boolean,
): MessageStatus? = when {
    !isOutgoing -> null
    sendingState is MessageSendingStatePending -> MessageStatus.Sending
    sendingState is MessageSendingStateFailed -> MessageStatus.Failed
    savedMessages -> null
    id <= lastReadOutboxMessageId -> MessageStatus.Read
    else -> MessageStatus.Sent
}

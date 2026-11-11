package com.xbot.kotogram.feature.chats.data.extensions

import io.xbot.tdlib.api.MessageChatAddMembers
import io.xbot.tdlib.api.MessageChatDeleteMember
import io.xbot.tdlib.api.MessageContent
import io.xbot.tdlib.api.MessageGiftedPremium
import io.xbot.tdlib.api.MessageInviteVideoChatParticipants

internal fun MessageContent.mentionedUserIds(): List<Long> = when (this) {
    is MessageChatAddMembers -> memberUserIds
    is MessageChatDeleteMember -> listOf(userId)
    is MessageInviteVideoChatParticipants -> userIds
    is MessageGiftedPremium -> listOf(gifterUserId, receiverUserId)
    else -> emptyList()
}

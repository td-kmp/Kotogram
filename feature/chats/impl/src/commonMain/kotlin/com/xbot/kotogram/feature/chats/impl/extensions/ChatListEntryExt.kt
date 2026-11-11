@file:Suppress("TooManyFunctions")

package com.xbot.kotogram.feature.chats.impl.extensions

import com.xbot.kotogram.core.designsystem.component.AvatarIcon
import com.xbot.kotogram.core.designsystem.component.AvatarShape
import com.xbot.kotogram.core.designsystem.component.MessageStatus
import com.xbot.kotogram.core.designsystem.component.TypingAction
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.StringResource
import com.xbot.kotogram.core.resources.chat_list_instantvideo
import com.xbot.kotogram.core.resources.chat_list_voice
import com.xbot.kotogram.core.resources.chatlist_secretchat_created
import com.xbot.kotogram.core.resources.chatlist_secretchat_exkeys
import com.xbot.kotogram.core.resources.chatlist_secretchat_joined
import com.xbot.kotogram.core.resources.chatlist_secretchat_terminated
import com.xbot.kotogram.core.resources.chatlist_you
import com.xbot.kotogram.feature.chats.domain.model.ChatListEntry
import com.xbot.kotogram.feature.chats.domain.model.ChatTyping
import com.xbot.kotogram.feature.chats.domain.model.ForumTopicEntry
import com.xbot.kotogram.feature.chats.domain.model.SpecialChat
import com.xbot.kotogram.feature.chats.impl.screen.list.AuthorName
import com.xbot.kotogram.feature.chats.impl.screen.list.AvatarBadgeModel
import com.xbot.kotogram.feature.chats.impl.screen.list.AvatarModel
import com.xbot.kotogram.feature.chats.impl.screen.list.ChatListRow
import com.xbot.kotogram.feature.chats.impl.screen.list.ChatPreview
import com.xbot.kotogram.feature.chats.impl.screen.list.PreviewText
import com.xbot.kotogram.feature.chats.impl.screen.list.RowTitle
import com.xbot.kotogram.feature.chats.impl.screen.list.StoriesModel
import com.xbot.kotogram.feature.chats.impl.screen.list.TagModel
import com.xbot.kotogram.feature.chats.impl.screen.list.TitleMarkModel
import com.xbot.kotogram.feature.chats.impl.screen.list.TopicIconModel
import com.xbot.kotogram.feature.chats.impl.screen.list.TopicModel
import com.xbot.kotogram.feature.chats.impl.screen.list.TypingKind
import com.xbot.kotogram.feature.chats.impl.screen.list.TypingModel
import io.xbot.tdlib.api.ActiveStoryStateLive
import io.xbot.tdlib.api.ActiveStoryStateRead
import io.xbot.tdlib.api.ActiveStoryStateUnread
import io.xbot.tdlib.api.Chat
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
import io.xbot.tdlib.api.ChatMemberStatusMember
import io.xbot.tdlib.api.ChatTypeBasicGroup
import io.xbot.tdlib.api.ChatTypePrivate
import io.xbot.tdlib.api.ChatTypeSecret
import io.xbot.tdlib.api.ChatTypeSupergroup
import io.xbot.tdlib.api.DraftMessage
import io.xbot.tdlib.api.DraftMessageContentText
import io.xbot.tdlib.api.DraftMessageContentVideoNote
import io.xbot.tdlib.api.DraftMessageContentVoiceNote
import io.xbot.tdlib.api.EmojiStatusTypeCustomEmoji
import io.xbot.tdlib.api.Message
import io.xbot.tdlib.api.MessageOriginChannel
import io.xbot.tdlib.api.MessageOriginChat
import io.xbot.tdlib.api.MessageOriginHiddenUser
import io.xbot.tdlib.api.MessageOriginUser
import io.xbot.tdlib.api.MessageSendingStateFailed
import io.xbot.tdlib.api.MessageSendingStatePending
import io.xbot.tdlib.api.MessageText
import io.xbot.tdlib.api.SecretChatStateClosed
import io.xbot.tdlib.api.SecretChatStatePending
import io.xbot.tdlib.api.SecretChatStateReady
import io.xbot.tdlib.api.UserStatusOnline
import io.xbot.tdlib.api.UserTypeBot
import io.xbot.tdlib.api.UserTypeDeleted
import io.xbot.tdlib.api.UserTypeRegular
import io.xbot.tdlib.api.VerificationStatus
import kotlin.time.Clock

internal fun ChatListEntry.toRow(): ChatListRow {
    val message = chat.lastMessage
    val draft = chat.draftMessage?.toPreview()
    val verification = user?.verificationStatus ?: supergroup?.verificationStatus
    val unread = chat.unreadCount > 0 || chat.isMarkedAsUnread
    val preview = draft ?: message?.let { preview(it) } ?: secretChatPreview() ?: ChatPreview.Empty
    return ChatListRow(
        chatId = chat.id,
        title = title(),
        avatar = avatar(),
        stories = stories(),
        avatarBadge = avatarBadge(),
        mark = mark(verification),
        emojiStatusId = emojiStatusId(verification),
        botVerificationEmojiId = verification?.botVerificationIconCustomEmojiId?.takeIf { it != 0L },
        isSecret = chat.type is ChatTypeSecret,
        isDirectMessages = supergroup?.isDirectMessagesGroup == true,
        isMuted = isMuted(),
        isPinned = position?.isPinned == true,
        date = message?.date ?: chat.draftMessage?.date ?: 0,
        status = message?.takeIf { draft == null }?.status(
            chat.lastReadOutboxMessageId,
            special == SpecialChat.SavedMessages,
        ),
        unreadCount = chat.unreadCount,
        hasUnreadMention = chat.unreadMentionCount > 0,
        hasUnreadReaction = chat.unreadReactionCount > 0,
        hasUnreadPollVotes = chat.unreadPollVoteCount > 0,
        isMarkedAsUnread = chat.isMarkedAsUnread,
        author = if (draft == null && (preview as? ChatPreview.Message)?.hideAuthor != true) author() else null,
        preview = preview,
        typing = typing(),
        topics = if (draft == null) topics.map { it.toModel() } else emptyList(),
        tags = tags.map { TagModel(it.name.text.text, it.colorId) },
        hasOpenAppButton = (user?.type as? UserTypeBot)?.hasMainWebApp == true && !unread,
    )
}

private fun ChatListEntry.emojiStatusId(verification: VerificationStatus?): Long? =
    (chat.emojiStatus?.type as? EmojiStatusTypeCustomEmoji)?.customEmojiId
        ?.takeIf {
            special != SpecialChat.SavedMessages && verification?.isScam != true && verification?.isFake != true
        }

private fun ChatListEntry.title(): RowTitle = when {
    special == SpecialChat.SavedMessages -> RowTitle.SavedMessages
    special == SpecialChat.Replies -> RowTitle.Replies
    user?.type is UserTypeDeleted -> RowTitle.DeletedAccount
    else -> RowTitle.Name(chat.title)
}

private fun ChatListEntry.avatar(): AvatarModel {
    val icon = when {
        special == SpecialChat.SavedMessages -> AvatarIcon.SavedMessages
        special == SpecialChat.Replies -> AvatarIcon.Replies
        user?.type is UserTypeDeleted -> AvatarIcon.Deleted
        else -> null
    }
    return AvatarModel(
        photo = chat.photo?.small,
        thumbnail = chat.photo?.minithumbnail?.data,
        letters = chat.letters(),
        colorIndex = chat.accentColorId,
        icon = icon,
        shape = if (supergroup?.isForum == true) AvatarShape.RoundedRect else AvatarShape.Circle,
    )
}

private fun ChatListEntry.stories(): StoriesModel? {
    val active = stories?.takeIf { it.stories.isNotEmpty() }
    val unseen = active?.stories?.filter { it.storyId > active.maxReadStoryId }.orEmpty()
    return when {
        special == SpecialChat.SavedMessages -> null

        active != null && active.stories.any { it.isLive } -> StoriesModel.Live

        active != null -> StoriesModel.Stories(active.stories.size, unseen.size, unseen.any { it.isForCloseFriends })

        else -> when (user?.activeStoryState ?: supergroup?.activeStoryState) {
            is ActiveStoryStateLive -> StoriesModel.Live
            is ActiveStoryStateUnread -> StoriesModel.Stories(1, 1, false)
            is ActiveStoryStateRead -> StoriesModel.Stories(1, 0, false)
            else -> null
        }
    }
}

private fun ChatListEntry.avatarBadge(): AvatarBadgeModel? {
    val user = user
    val online = user != null &&
        user.type is UserTypeRegular &&
        !user.isSupport &&
        user.id != myUserId &&
        special != SpecialChat.ServiceNotifications &&
        (user.status as? UserStatusOnline)?.expires?.let { it > Clock.System.now().epochSeconds } == true
    val videoChat = chat.type !is ChatTypePrivate && chat.type !is ChatTypeSecret &&
        chat.videoChat.groupCallId != 0 && chat.videoChat.hasParticipants
    val subscription = ((supergroup?.status as? ChatMemberStatusMember)?.memberUntilDate ?: 0) > 0
    return when {
        online -> AvatarBadgeModel.Online
        videoChat -> AvatarBadgeModel.VoiceChat
        chat.messageAutoDeleteTime > 0 -> AvatarBadgeModel.AutoDelete(chat.messageAutoDeleteTime)
        subscription -> AvatarBadgeModel.Subscription
        else -> null
    }
}

private fun ChatListEntry.mark(verification: VerificationStatus?): TitleMarkModel? {
    if (special == SpecialChat.SavedMessages) return null
    return when {
        verification?.isVerified == true -> TitleMarkModel.Verified
        verification?.isScam == true -> TitleMarkModel.Scam
        verification?.isFake == true -> TitleMarkModel.Fake
        chat.emojiStatus != null -> null
        user?.isPremium == true -> TitleMarkModel.Premium
        else -> null
    }
}

private fun ChatListEntry.isMuted(): Boolean {
    val settings = chat.notificationSettings
    val muteFor = if (settings.useDefaultMuteFor) scopeSettings?.muteFor ?: 0 else settings.muteFor
    return muteFor > 0
}

private val ChatListEntry.isGroup: Boolean
    get() = chat.type is ChatTypeBasicGroup || (chat.type as? ChatTypeSupergroup)?.isChannel == false

private val ChatListEntry.isChannel: Boolean
    get() = (chat.type as? ChatTypeSupergroup)?.isChannel == true

private fun ChatListEntry.author(): AuthorName? {
    val message = chat.lastMessage
    return when {
        message == null -> null
        special == SpecialChat.SavedMessages -> forwardAuthor()?.let(AuthorName::Name)
        !isGroup || senderChat?.id == chat.id -> null
        message.isOutgoing -> AuthorName.You
        else -> (senderUser?.fullName() ?: senderChat?.title)?.let(AuthorName::Name)
    }
}

private fun ChatListEntry.forwardAuthor(): String? = when (val origin = chat.lastMessage?.forwardInfo?.origin) {
    is MessageOriginUser -> senderUser?.takeIf { it.id == origin.senderUserId }?.fullName()
    is MessageOriginHiddenUser -> origin.senderName
    is MessageOriginChat -> origin.authorSignature.ifEmpty { null }
    is MessageOriginChannel -> origin.authorSignature.ifEmpty { null }
    else -> null
}

private fun ChatListEntry.preview(message: Message): ChatPreview {
    val you: StringResource = StringResource.Text(Res.string.chatlist_you)
    val sender: Any = when {
        message.isOutgoing -> you
        else -> senderUser?.fullName() ?: senderChat?.title ?: chat.title
    }
    val preview = message.toPreview(
        PreviewContext(
            sender = sender,
            isOutgoing = message.isOutgoing,
            isChannel = isChannel,
            isPrivate = chat.type is ChatTypePrivate || chat.type is ChatTypeSecret,
            showMedia = chat.type !is ChatTypeSecret,
            users = mentionedUsers,
            myUserId = myUserId,
            you = you,
        ),
    )
    val hiddenCode = special == SpecialChat.ServiceNotifications || special == SpecialChat.VerificationCodes
    val content = preview.content
    return if (hiddenCode && content is PreviewText.Formatted && message.content is MessageText) {
        ChatPreview.Message(
            PreviewText.Formatted(content.text, hiddenCode = true),
            preview.icon,
            preview.thumbnails,
            preview.hideAuthor,
        )
    } else {
        preview
    }
}

private fun ChatListEntry.secretChatPreview(): ChatPreview? {
    val secret = secretChat ?: return null
    val name = user?.firstName.orEmpty()
    val text = when (secret.state) {
        SecretChatStatePending -> StringResource.Text(Res.string.chatlist_secretchat_exkeys)

        SecretChatStateReady -> if (secret.isOutbound) {
            StringResource.Text(Res.string.chatlist_secretchat_joined, name)
        } else {
            StringResource.Text(Res.string.chatlist_secretchat_created, name)
        }

        SecretChatStateClosed -> StringResource.Text(Res.string.chatlist_secretchat_terminated)
    }
    return ChatPreview.Message(PreviewText.Resource(text), null, emptyList(), hideAuthor = true)
}

private fun ChatListEntry.typing(): TypingModel? {
    val actors = typing.filter { it.user?.id != myUserId }.mapNotNull { typing -> typing.kind()?.let { typing to it } }
    if (actors.isEmpty()) return null
    val private = chat.type is ChatTypePrivate || chat.type is ChatTypeSecret
    val kinds = actors.map { it.second }.distinct()
    val kind = kinds.singleOrNull() ?: TypingKind.Text
    return TypingModel(
        names = actors.map { (typing, _) -> typing.user?.firstName?.ifEmpty { null } ?: typing.chat?.title.orEmpty() },
        action = kind.action(),
        kind = kind,
        inPrivateChat = private && actors.size == 1,
    )
}

internal fun ChatListEntry.hasSameInputsAs(other: ChatListEntry) = hasSamePeersAs(other) && hasSameActivityAs(other)

private fun ChatListEntry.hasSamePeersAs(other: ChatListEntry) = chat === other.chat &&
    position === other.position &&
    senderUser === other.senderUser &&
    senderChat === other.senderChat &&
    user === other.user &&
    supergroup === other.supergroup &&
    secretChat === other.secretChat &&
    special == other.special &&
    myUserId == other.myUserId

private fun ChatListEntry.hasSameActivityAs(other: ChatListEntry) = stories === other.stories &&
    scopeSettings === other.scopeSettings &&
    tags.hasSameItemsAs(other.tags) &&
    mentionedUsers.hasSameItemsAs(other.mentionedUsers) &&
    typing.map { it.action }.hasSameItemsAs(other.typing.map { it.action }) &&
    typing.map { it.user }.hasSameItemsAs(other.typing.map { it.user }) &&
    topics.map { it.info }.hasSameItemsAs(other.topics.map { it.info }) &&
    topics.map { it.isUnread } == other.topics.map { it.isUnread }

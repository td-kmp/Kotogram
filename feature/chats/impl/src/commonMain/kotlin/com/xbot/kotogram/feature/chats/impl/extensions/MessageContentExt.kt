@file:Suppress("TooManyFunctions")

package com.xbot.kotogram.feature.chats.impl.extensions

import com.xbot.kotogram.core.designsystem.component.MessageIcon
import com.xbot.kotogram.core.designsystem.component.MessageThumbnailType
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.StringResource
import com.xbot.kotogram.core.resources.chat_list_contact
import com.xbot.kotogram.core.resources.chat_list_gif
import com.xbot.kotogram.core.resources.chat_list_instantvideo
import com.xbot.kotogram.core.resources.chat_list_map
import com.xbot.kotogram.core.resources.chat_list_photo1
import com.xbot.kotogram.core.resources.chat_list_service_paymentsent
import com.xbot.kotogram.core.resources.chat_list_sticker
import com.xbot.kotogram.core.resources.chat_list_video
import com.xbot.kotogram.core.resources.chat_list_video1
import com.xbot.kotogram.core.resources.chat_list_voice
import com.xbot.kotogram.core.resources.chat_livelocation
import com.xbot.kotogram.core.resources.chat_service_botwriteallowed
import com.xbot.kotogram.core.resources.chat_service_channel_disabledtimer
import com.xbot.kotogram.core.resources.chat_service_channel_removedphoto
import com.xbot.kotogram.core.resources.chat_service_channel_settimer
import com.xbot.kotogram.core.resources.chat_service_channel_updatedphoto
import com.xbot.kotogram.core.resources.chat_service_channel_updatedtitle
import com.xbot.kotogram.core.resources.chat_service_disabledtheme
import com.xbot.kotogram.core.resources.chat_service_disabledtheme_you
import com.xbot.kotogram.core.resources.chat_service_group_addedmembers1
import com.xbot.kotogram.core.resources.chat_service_group_addedself
import com.xbot.kotogram.core.resources.chat_service_group_created1
import com.xbot.kotogram.core.resources.chat_service_group_disabledtimer
import com.xbot.kotogram.core.resources.chat_service_group_joinedbylink
import com.xbot.kotogram.core.resources.chat_service_group_migratedtosupergroup
import com.xbot.kotogram.core.resources.chat_service_group_removedmembers1
import com.xbot.kotogram.core.resources.chat_service_group_removedphoto
import com.xbot.kotogram.core.resources.chat_service_group_removedself
import com.xbot.kotogram.core.resources.chat_service_group_settimer
import com.xbot.kotogram.core.resources.chat_service_group_tookscreenshot
import com.xbot.kotogram.core.resources.chat_service_group_topiccreated
import com.xbot.kotogram.core.resources.chat_service_group_topicedited_hided
import com.xbot.kotogram.core.resources.chat_service_group_topicedited_paused
import com.xbot.kotogram.core.resources.chat_service_group_topicedited_resumed
import com.xbot.kotogram.core.resources.chat_service_group_topicedited_title
import com.xbot.kotogram.core.resources.chat_service_group_topicedited_unhided
import com.xbot.kotogram.core.resources.chat_service_group_topicedited_you_hided
import com.xbot.kotogram.core.resources.chat_service_group_topicedited_you_paused
import com.xbot.kotogram.core.resources.chat_service_group_topicedited_you_resumed
import com.xbot.kotogram.core.resources.chat_service_group_topicedited_you_title
import com.xbot.kotogram.core.resources.chat_service_group_topicedited_you_unhided
import com.xbot.kotogram.core.resources.chat_service_group_updatedphoto
import com.xbot.kotogram.core.resources.chat_service_group_updatedpinnedmessage1
import com.xbot.kotogram.core.resources.chat_service_group_updatedtitle1
import com.xbot.kotogram.core.resources.chat_service_incominggroupcall
import com.xbot.kotogram.core.resources.chat_service_missedgroupcall
import com.xbot.kotogram.core.resources.chat_service_outgoinggroupcall
import com.xbot.kotogram.core.resources.chat_service_peerjoinedtelegram
import com.xbot.kotogram.core.resources.chat_service_secretchat_disabledtimer1
import com.xbot.kotogram.core.resources.chat_service_secretchat_disabledtimer_self1
import com.xbot.kotogram.core.resources.chat_service_secretchat_settimer1
import com.xbot.kotogram.core.resources.chat_service_secretchat_settimer_self1
import com.xbot.kotogram.core.resources.chat_service_stargift_from
import com.xbot.kotogram.core.resources.chat_service_storymentioned
import com.xbot.kotogram.core.resources.chat_service_storymentioned_you
import com.xbot.kotogram.core.resources.chat_service_suggestedphoto
import com.xbot.kotogram.core.resources.chat_service_updatetheme
import com.xbot.kotogram.core.resources.chat_service_updatetheme_you
import com.xbot.kotogram.core.resources.chat_service_voicechatfinished
import com.xbot.kotogram.core.resources.chat_service_voicechatfinishedyou
import com.xbot.kotogram.core.resources.chatlist_giveaway
import com.xbot.kotogram.core.resources.chatlist_service_call_cancelled
import com.xbot.kotogram.core.resources.chatlist_service_call_incoming
import com.xbot.kotogram.core.resources.chatlist_service_call_missed
import com.xbot.kotogram.core.resources.chatlist_service_call_outgoing
import com.xbot.kotogram.core.resources.chatlist_service_destructingphoto
import com.xbot.kotogram.core.resources.chatlist_service_destructingvideo
import com.xbot.kotogram.core.resources.chatlist_service_videocall_cancelled
import com.xbot.kotogram.core.resources.chatlist_service_videocall_incoming
import com.xbot.kotogram.core.resources.chatlist_service_videocall_missed
import com.xbot.kotogram.core.resources.chatlist_service_videocall_outgoing
import com.xbot.kotogram.core.resources.chatlist_service_voicechatfinished_channel
import com.xbot.kotogram.core.resources.chatlist_service_voicechatinvitation
import com.xbot.kotogram.core.resources.chatlist_service_voicechatinvitationbyyou
import com.xbot.kotogram.core.resources.chatlist_service_voicechatinvitationforyou
import com.xbot.kotogram.core.resources.chatlist_service_voicechatscheduled
import com.xbot.kotogram.core.resources.chatlist_service_voicechatscheduled_channel
import com.xbot.kotogram.core.resources.chatlist_service_voicechatscheduledyou
import com.xbot.kotogram.core.resources.chatlist_service_voicechatstarted
import com.xbot.kotogram.core.resources.chatlist_service_voicechatstarted_channel
import com.xbot.kotogram.core.resources.chatlist_service_voicechatstartedyou
import com.xbot.kotogram.core.resources.chatlist_story
import com.xbot.kotogram.core.resources.chatlist_unsupportedmessage
import com.xbot.kotogram.core.resources.chatservice_channelcreated
import com.xbot.kotogram.core.resources.inputattach_audio
import com.xbot.kotogram.core.resources.inputattach_popover_file
import com.xbot.kotogram.core.resources.newpoll_attachsticker
import com.xbot.kotogram.core.resources.peerinfo_action_message
import com.xbot.kotogram.core.resources.timer_days
import com.xbot.kotogram.core.resources.timer_hours
import com.xbot.kotogram.core.resources.timer_minutes
import com.xbot.kotogram.core.resources.timer_months
import com.xbot.kotogram.core.resources.timer_seconds
import com.xbot.kotogram.feature.chats.impl.screen.list.ChatPreview
import com.xbot.kotogram.feature.chats.impl.screen.list.PreviewText
import com.xbot.kotogram.feature.chats.impl.screen.list.ThumbnailModel
import io.xbot.tdlib.api.CallDiscardReasonDeclined
import io.xbot.tdlib.api.CallDiscardReasonMissed
import io.xbot.tdlib.api.FormattedText
import io.xbot.tdlib.api.Message
import io.xbot.tdlib.api.MessageAnimatedEmoji
import io.xbot.tdlib.api.MessageAnimation
import io.xbot.tdlib.api.MessageAudio
import io.xbot.tdlib.api.MessageBasicGroupChatCreate
import io.xbot.tdlib.api.MessageBotWriteAccessAllowed
import io.xbot.tdlib.api.MessageCall
import io.xbot.tdlib.api.MessageChatAddMembers
import io.xbot.tdlib.api.MessageChatChangePhoto
import io.xbot.tdlib.api.MessageChatChangeTitle
import io.xbot.tdlib.api.MessageChatDeleteMember
import io.xbot.tdlib.api.MessageChatDeletePhoto
import io.xbot.tdlib.api.MessageChatJoinByLink
import io.xbot.tdlib.api.MessageChatJoinByRequest
import io.xbot.tdlib.api.MessageChatSetMessageAutoDeleteTime
import io.xbot.tdlib.api.MessageChatSetTheme
import io.xbot.tdlib.api.MessageChatUpgradeFrom
import io.xbot.tdlib.api.MessageChatUpgradeTo
import io.xbot.tdlib.api.MessageChecklist
import io.xbot.tdlib.api.MessageContact
import io.xbot.tdlib.api.MessageContactRegistered
import io.xbot.tdlib.api.MessageContent
import io.xbot.tdlib.api.MessageCustomServiceAction
import io.xbot.tdlib.api.MessageDice
import io.xbot.tdlib.api.MessageDocument
import io.xbot.tdlib.api.MessageExpiredPhoto
import io.xbot.tdlib.api.MessageExpiredVideo
import io.xbot.tdlib.api.MessageExpiredVideoNote
import io.xbot.tdlib.api.MessageExpiredVoiceNote
import io.xbot.tdlib.api.MessageForumTopicCreated
import io.xbot.tdlib.api.MessageForumTopicEdited
import io.xbot.tdlib.api.MessageForumTopicIsClosedToggled
import io.xbot.tdlib.api.MessageForumTopicIsHiddenToggled
import io.xbot.tdlib.api.MessageGame
import io.xbot.tdlib.api.MessageGift
import io.xbot.tdlib.api.MessageGiftedPremium
import io.xbot.tdlib.api.MessageGiftedStars
import io.xbot.tdlib.api.MessageGiveaway
import io.xbot.tdlib.api.MessageGiveawayCompleted
import io.xbot.tdlib.api.MessageGiveawayCreated
import io.xbot.tdlib.api.MessageGiveawayWinners
import io.xbot.tdlib.api.MessageGroupCall
import io.xbot.tdlib.api.MessageInviteVideoChatParticipants
import io.xbot.tdlib.api.MessageInvoice
import io.xbot.tdlib.api.MessageLiveLocation
import io.xbot.tdlib.api.MessageLocation
import io.xbot.tdlib.api.MessagePaidMedia
import io.xbot.tdlib.api.MessagePaymentSuccessful
import io.xbot.tdlib.api.MessagePhoto
import io.xbot.tdlib.api.MessagePinMessage
import io.xbot.tdlib.api.MessagePoll
import io.xbot.tdlib.api.MessagePremiumGiftCode
import io.xbot.tdlib.api.MessageReplyToStory
import io.xbot.tdlib.api.MessageScreenshotTaken
import io.xbot.tdlib.api.MessageSticker
import io.xbot.tdlib.api.MessageStory
import io.xbot.tdlib.api.MessageSuggestProfilePhoto
import io.xbot.tdlib.api.MessageSupergroupChatCreate
import io.xbot.tdlib.api.MessageText
import io.xbot.tdlib.api.MessageUnsupported
import io.xbot.tdlib.api.MessageUpgradedGift
import io.xbot.tdlib.api.MessageVenue
import io.xbot.tdlib.api.MessageVideo
import io.xbot.tdlib.api.MessageVideoChatEnded
import io.xbot.tdlib.api.MessageVideoChatScheduled
import io.xbot.tdlib.api.MessageVideoChatStarted
import io.xbot.tdlib.api.MessageVideoNote
import io.xbot.tdlib.api.MessageVoiceNote
import io.xbot.tdlib.api.PaidMedia
import io.xbot.tdlib.api.PaidMediaPhoto
import io.xbot.tdlib.api.PaidMediaPreview
import io.xbot.tdlib.api.PaidMediaVideo
import io.xbot.tdlib.api.Photo
import io.xbot.tdlib.api.User

internal class PreviewContext(
    val sender: Any,
    val isOutgoing: Boolean,
    val isChannel: Boolean,
    val isPrivate: Boolean,
    val showMedia: Boolean,
    val users: List<User>,
    val myUserId: Long,
    val you: StringResource,
)

internal fun Message.toPreview(context: PreviewContext): ChatPreview.Message {
    val content = content
    val service = content.serviceText(context, this)
    if (service != null) {
        return ChatPreview.Message(
            PreviewText.Resource(service),
            content.icon(this),
            [],
            content.keepsAuthor().not(),
        )
    }
    val thumbnails = if (context.showMedia && selfDestructType == null) content.thumbnails() else []
    return ChatPreview.Message(
        content.text(this),
        content.icon(this),
        thumbnails,
        hideAuthor =
            content is MessageCall && context.isPrivate,
    )
}

private fun MessageContent.keepsAuthor(): Boolean = this is MessageForumTopicCreated || this is MessageForumTopicEdited

@Suppress("CyclomaticComplexMethod")
private fun MessageContent.text(message: Message): PreviewText = when (this) {
    is MessageText -> PreviewText.Formatted(text)

    is MessagePhoto -> caption.orLabel(StringResource.Plural(Res.plurals.chat_list_photo1, 1, 1))

    is MessageVideo -> caption.orLabel(StringResource.Text(Res.string.chat_list_video))

    is MessageAnimation -> caption.orLabel(StringResource.Text(Res.string.chat_list_gif))

    is MessageVoiceNote -> caption.orLabel(StringResource.Text(Res.string.chat_list_voice))

    is MessageVideoNote -> PreviewText.Resource(StringResource.Text(Res.string.chat_list_instantvideo))

    is MessageAudio -> if (caption.text.isNotEmpty()) PreviewText.Formatted(caption) else audioTitle()

    is MessageDocument -> when {
        caption.text.isNotEmpty() -> PreviewText.Formatted(caption)
        document.fileName.isNotEmpty() -> PreviewText.Plain(document.fileName)
        else -> PreviewText.Resource(StringResource.Text(Res.string.inputattach_popover_file))
    }

    is MessageSticker -> if (sticker.emoji.isNotEmpty()) {
        PreviewText.Resource(StringResource.Text(Res.string.chat_list_sticker, sticker.emoji))
    } else {
        PreviewText.Resource(StringResource.Text(Res.string.newpoll_attachsticker))
    }

    is MessagePaidMedia -> caption.orLabel(paidMediaLabel())

    is MessageLocation, is MessageVenue -> PreviewText.Resource(StringResource.Text(Res.string.chat_list_map))

    is MessageLiveLocation -> PreviewText.Resource(StringResource.Text(Res.string.chat_livelocation))

    is MessageContact -> PreviewText.Resource(StringResource.Text(Res.string.chat_list_contact))

    is MessageAnimatedEmoji -> PreviewText.Plain(emoji)

    is MessageDice -> PreviewText.Plain(emoji)

    is MessageGame -> PreviewText.Plain(game.title)

    is MessagePoll -> PreviewText.Formatted(poll.question)

    is MessageChecklist -> PreviewText.Formatted(list.title)

    is MessageInvoice -> PreviewText.Plain(productInfo.title)

    is MessageStory -> PreviewText.Resource(StringResource.Text(Res.string.chatlist_story))

    is MessageGiveaway, is MessageGiveawayCreated, is MessageGiveawayCompleted, is MessageGiveawayWinners ->
        PreviewText.Resource(StringResource.Text(Res.string.chatlist_giveaway))

    is MessageExpiredPhoto, is MessageExpiredVoiceNote ->
        PreviewText.Resource(StringResource.Text(Res.string.chatlist_service_destructingphoto))

    is MessageExpiredVideo, is MessageExpiredVideoNote ->
        PreviewText.Resource(StringResource.Text(Res.string.chatlist_service_destructingvideo))

    is MessageCall -> PreviewText.Resource(callText(message.isOutgoing))

    is MessageUnsupported -> PreviewText.Resource(StringResource.Text(Res.string.chatlist_unsupportedmessage))

    else -> PreviewText.Resource(StringResource.Text(Res.string.peerinfo_action_message))
}

private fun FormattedText.orLabel(label: StringResource): PreviewText =
    if (text.isNotEmpty()) PreviewText.Formatted(this) else PreviewText.Resource(label)

private fun MessageAudio.audioTitle(): PreviewText = when {
    audio.title.isNotEmpty() && audio.performer.isNotEmpty() -> PreviewText.Plain("${audio.title} — ${audio.performer}")
    audio.title.isNotEmpty() -> PreviewText.Plain(audio.title)
    audio.performer.isNotEmpty() -> PreviewText.Plain(audio.performer)
    audio.fileName.isNotEmpty() -> PreviewText.Plain(audio.fileName)
    else -> PreviewText.Resource(StringResource.Text(Res.string.inputattach_audio))
}

private fun MessagePaidMedia.paidMediaLabel(): StringResource {
    val videos = media.count { it is PaidMediaVideo || (it is PaidMediaPreview && it.duration > 0) }
    return if (videos == media.size && videos > 0) {
        StringResource.Plural(Res.plurals.chat_list_video1, videos, videos)
    } else {
        StringResource.Plural(Res.plurals.chat_list_photo1, media.size.coerceAtLeast(1), media.size)
    }
}

private fun MessageCall.callText(outgoing: Boolean): StringResource {
    val missed = discardReason is CallDiscardReasonMissed || discardReason is CallDiscardReasonDeclined || duration == 0
    val finished = when {
        isVideo && outgoing -> Res.string.chatlist_service_videocall_outgoing
        isVideo -> Res.string.chatlist_service_videocall_incoming
        outgoing -> Res.string.chatlist_service_call_outgoing
        else -> Res.string.chatlist_service_call_incoming
    }
    return when {
        duration > 0 -> StringResource.Text(finished, duration.callDuration())
        missed && !outgoing && isVideo -> StringResource.Text(Res.string.chatlist_service_videocall_missed)
        missed && !outgoing -> StringResource.Text(Res.string.chatlist_service_call_missed)
        isVideo -> StringResource.Text(Res.string.chatlist_service_videocall_cancelled)
        else -> StringResource.Text(Res.string.chatlist_service_call_cancelled)
    }
}

private fun Int.callDuration(): String {
    val hours = this / SECONDS_IN_HOUR
    val minutes = this % SECONDS_IN_HOUR / SECONDS_IN_MINUTE
    val seconds = this % SECONDS_IN_MINUTE
    val tail = seconds.toString().padStart(2, '0')
    return if (hours > 0) "$hours:${minutes.toString().padStart(2, '0')}:$tail" else "$minutes:$tail"
}

@Suppress("CyclomaticComplexMethod", "LongMethod")
private fun MessageContent.serviceText(
    context: PreviewContext,
    message: Message,
): StringResource? {
    val sender = context.sender
    val you = message.isOutgoing
    return when (this) {
        is MessageBasicGroupChatCreate -> text(Res.string.chat_service_group_created1, sender, title)

        is MessageSupergroupChatCreate -> if (context.isChannel) {
            text(Res.string.chatservice_channelcreated)
        } else {
            text(Res.string.chat_service_group_created1, sender, title)
        }

        is MessageChatChangeTitle -> if (context.isChannel) {
            text(Res.string.chat_service_channel_updatedtitle, title)
        } else {
            text(Res.string.chat_service_group_updatedtitle1, sender, title)
        }

        is MessageChatChangePhoto -> if (context.isChannel) {
            text(Res.string.chat_service_channel_updatedphoto)
        } else {
            text(Res.string.chat_service_group_updatedphoto, sender)
        }

        is MessageChatDeletePhoto -> if (context.isChannel) {
            text(Res.string.chat_service_channel_removedphoto)
        } else {
            text(Res.string.chat_service_group_removedphoto, sender)
        }

        is MessageChatAddMembers -> {
            val self = memberUserIds.singleOrNull() == message.senderUserId()
            if (self) {
                text(Res.string.chat_service_group_addedself, sender)
            } else {
                text(Res.string.chat_service_group_addedmembers1, sender, context.names(memberUserIds))
            }
        }

        is MessageChatJoinByLink -> text(Res.string.chat_service_group_joinedbylink, sender)

        is MessageChatJoinByRequest -> text(Res.string.chat_service_group_addedself, sender)

        is MessageChatDeleteMember -> if (userId == message.senderUserId()) {
            text(Res.string.chat_service_group_removedself, sender)
        } else {
            text(Res.string.chat_service_group_removedmembers1, sender, context.names([userId]))
        }

        is MessageChatUpgradeTo, is MessageChatUpgradeFrom -> text(Res.string.chat_service_group_migratedtosupergroup)

        is MessagePinMessage -> text(Res.string.chat_service_group_updatedpinnedmessage1, sender, ELLIPSIS)

        is MessageScreenshotTaken -> text(Res.string.chat_service_group_tookscreenshot, sender)

        is MessageChatSetTheme -> when {
            theme == null && you -> text(Res.string.chat_service_disabledtheme_you)
            theme == null -> text(Res.string.chat_service_disabledtheme, sender)
            you -> text(Res.string.chat_service_updatetheme_you, THEME_EMOJI)
            else -> text(Res.string.chat_service_updatetheme, sender, THEME_EMOJI)
        }

        is MessageChatSetMessageAutoDeleteTime -> autoDeleteText(context, sender, you)

        is MessageForumTopicCreated -> text(Res.string.chat_service_group_topiccreated, sender, name)

        is MessageForumTopicEdited -> if (you) {
            text(Res.string.chat_service_group_topicedited_you_title, name)
        } else {
            text(Res.string.chat_service_group_topicedited_title, sender, name)
        }

        is MessageForumTopicIsClosedToggled -> when {
            isClosed && you -> text(Res.string.chat_service_group_topicedited_you_paused)
            isClosed -> text(Res.string.chat_service_group_topicedited_paused, sender)
            you -> text(Res.string.chat_service_group_topicedited_you_resumed)
            else -> text(Res.string.chat_service_group_topicedited_resumed, sender)
        }

        is MessageForumTopicIsHiddenToggled -> when {
            isHidden && you -> text(Res.string.chat_service_group_topicedited_you_hided)
            isHidden -> text(Res.string.chat_service_group_topicedited_hided, sender)
            you -> text(Res.string.chat_service_group_topicedited_you_unhided)
            else -> text(Res.string.chat_service_group_topicedited_unhided, sender)
        }

        is MessageVideoChatStarted -> when {
            context.isChannel -> text(Res.string.chatlist_service_voicechatstarted_channel)
            you -> text(Res.string.chatlist_service_voicechatstartedyou)
            else -> text(Res.string.chatlist_service_voicechatstarted, sender)
        }

        is MessageVideoChatEnded -> when {
            context.isChannel -> text(Res.string.chatlist_service_voicechatfinished_channel, duration.callDuration())
            you -> text(Res.string.chat_service_voicechatfinishedyou, duration.callDuration())
            else -> text(Res.string.chat_service_voicechatfinished, sender, duration.callDuration())
        }

        is MessageVideoChatScheduled -> when {
            context.isChannel -> text(Res.string.chatlist_service_voicechatscheduled_channel, DateArgument(startDate))
            you -> text(Res.string.chatlist_service_voicechatscheduledyou, "", DateArgument(startDate))
            else -> text(Res.string.chatlist_service_voicechatscheduled, sender, DateArgument(startDate))
        }

        is MessageInviteVideoChatParticipants -> when {
            you -> text(Res.string.chatlist_service_voicechatinvitationbyyou, context.names(userIds))
            context.myUserId in userIds -> text(Res.string.chatlist_service_voicechatinvitationforyou, sender)
            else -> text(Res.string.chatlist_service_voicechatinvitation, sender, context.names(userIds))
        }

        is MessageGroupCall -> text(
            when {
                wasMissed -> Res.string.chat_service_missedgroupcall
                message.isOutgoing -> Res.string.chat_service_outgoinggroupcall
                else -> Res.string.chat_service_incominggroupcall
            },
        )

        is MessageContactRegistered -> text(Res.string.chat_service_peerjoinedtelegram, sender)

        is MessageCustomServiceAction -> StringResource.String(text)

        is MessagePaymentSuccessful -> text(Res.string.chat_list_service_paymentsent, "$totalAmount $currency")

        is MessageGift,
        is MessageUpgradedGift,
        is MessageGiftedPremium,
        is MessageGiftedStars,
        is MessagePremiumGiftCode,
        -> text(Res.string.chat_service_stargift_from, sender)

        is MessageSuggestProfilePhoto -> text(Res.string.chat_service_suggestedphoto, sender)

        is MessageBotWriteAccessAllowed -> text(Res.string.chat_service_botwriteallowed)

        is MessageStory -> if (viaMention) {
            if (you) text(
                Res.string.chat_service_storymentioned_you,
                sender,
            ) else text(Res.string.chat_service_storymentioned, sender)
        } else {
            null
        }

        else -> null
    }
}

private fun MessageChatSetMessageAutoDeleteTime.autoDeleteText(
    context: PreviewContext,
    sender: Any,
    you: Boolean,
): StringResource {
    val time = messageAutoDeleteTime.timerResource()
    return when {
        context.isChannel && time == null -> text(Res.string.chat_service_channel_disabledtimer)
        context.isChannel -> text(Res.string.chat_service_channel_settimer, time!!)
        !context.isPrivate && time == null -> text(Res.string.chat_service_group_disabledtimer)
        !context.isPrivate -> text(Res.string.chat_service_group_settimer, time!!)
        time == null && you -> text(Res.string.chat_service_secretchat_disabledtimer_self1)
        time == null -> text(Res.string.chat_service_secretchat_disabledtimer1, sender)
        you -> text(Res.string.chat_service_secretchat_settimer_self1, time)
        else -> text(Res.string.chat_service_secretchat_settimer1, sender, time)
    }
}

internal fun Int.timerResource(): StringResource? = when {
    this <= 0 -> null

    this % SECONDS_IN_MONTH == 0 -> StringResource.Plural(
        Res.plurals.timer_months,
        this / SECONDS_IN_MONTH,
        this / SECONDS_IN_MONTH,
    )

    this % SECONDS_IN_DAY == 0 -> StringResource.Plural(
        Res.plurals.timer_days,
        this / SECONDS_IN_DAY,
        this / SECONDS_IN_DAY,
    )

    this % SECONDS_IN_HOUR == 0 -> StringResource.Plural(
        Res.plurals.timer_hours,
        this / SECONDS_IN_HOUR,
        this / SECONDS_IN_HOUR,
    )

    this % SECONDS_IN_MINUTE == 0 ->
        StringResource.Plural(Res.plurals.timer_minutes, this / SECONDS_IN_MINUTE, this / SECONDS_IN_MINUTE)

    else -> StringResource.Plural(Res.plurals.timer_seconds, this, this)
}

private fun text(
    resource: org.jetbrains.compose.resources.StringResource,
    vararg args: Any,
): StringResource = StringResource.Text(resource, *args)

private fun Message.senderUserId(): Long = (senderId as? io.xbot.tdlib.api.MessageSenderUser)?.userId ?: 0L

private fun PreviewContext.names(ids: List<Long>): Any {
    val names = ids.map { id -> if (id == myUserId) you else users.firstOrNull { it.id == id }?.fullName().orEmpty() }
    return names.singleOrNull() ?: NameList(names)
}

internal class NameList(
    val names: List<Any>,
)

internal class DateArgument(
    val epochSeconds: Int,
)

private fun MessageContent.icon(message: Message): MessageIcon? = when {
    message.forwardInfo != null && message.importInfo == null -> MessageIcon.Forwarded

    message.replyTo is MessageReplyToStory -> MessageIcon.StoryReply

    this is MessageVoiceNote -> MessageIcon.VoiceMessage

    this is MessageAudio -> MessageIcon.Audio

    this is MessagePoll -> MessageIcon.Poll

    this is MessageChecklist -> MessageIcon.Todo

    this is MessageGame -> MessageIcon.Game

    this is MessageLocation || this is MessageVenue || this is MessageLiveLocation -> MessageIcon.Location

    this is MessageCall -> when {
        isVideo && message.isOutgoing -> MessageIcon.OutgoingVideoCall
        isVideo -> MessageIcon.IncomingVideoCall
        message.isOutgoing -> MessageIcon.OutgoingCall
        else -> MessageIcon.IncomingCall
    }

    this is MessageGift || this is MessageUpgradedGift -> MessageIcon.Gift

    this is MessageGiftedPremium || this is MessageGiftedStars -> MessageIcon.Gift

    else -> null
}

private fun MessageContent.thumbnails(): List<ThumbnailModel> = when (this) {
    is MessagePhoto -> listOfNotNull(
        photo.preview()?.let {
            ThumbnailModel(it, MessageThumbnailType.Photo, hasSpoiler)
        },
    )

    is MessageVideo -> thumbnail(
        video.thumbnail?.file ?: video.minithumbnail?.data,
        MessageThumbnailType.Video,
        hasSpoiler,
    )

    is MessageAnimation -> thumbnail(
        animation.thumbnail?.file ?: animation.minithumbnail?.data,
        MessageThumbnailType.Video,
        hasSpoiler,
    )

    is MessageVideoNote -> thumbnail(
        videoNote.thumbnail?.file ?: videoNote.minithumbnail?.data,
        MessageThumbnailType.RoundVideo,
        spoiler = false,
    )

    is MessageDocument -> thumbnail(
        document.thumbnail?.file ?: document.minithumbnail?.data,
        MessageThumbnailType.Photo,
        spoiler = false,
    )

    is MessagePaidMedia -> media.take(MAX_THUMBNAILS).mapNotNull { it.thumbnail() }

    else -> []
}

private fun thumbnail(
    image: Any?,
    type: MessageThumbnailType,
    spoiler: Boolean,
): List<ThumbnailModel> = listOfNotNull(image?.let { ThumbnailModel(it, type, spoiler) })

private fun PaidMedia.thumbnail(): ThumbnailModel? = when (this) {
    is PaidMediaPhoto -> photo.preview()?.let { ThumbnailModel(it, MessageThumbnailType.Photo, false) }

    is PaidMediaVideo -> video.thumbnail?.file?.let { ThumbnailModel(it, MessageThumbnailType.Video, false) }

    is PaidMediaPreview -> minithumbnail?.data?.let {
        ThumbnailModel(it, if (duration > 0) MessageThumbnailType.Video else MessageThumbnailType.Photo, true)
    }

    else -> null
}

private fun Photo.preview(): Any? = sizes.minByOrNull { it.width }?.photo ?: minithumbnail?.data

internal fun User.fullName(): String = [firstName, lastName].filter { it.isNotBlank() }.joinToString(" ")

private const val SECONDS_IN_MINUTE = 60
private const val SECONDS_IN_HOUR = 3_600
private const val SECONDS_IN_DAY = 86_400
private const val SECONDS_IN_MONTH = 2_592_000
private const val MAX_THUMBNAILS = 3
private const val ELLIPSIS = "…"
private const val THEME_EMOJI = "🎨"

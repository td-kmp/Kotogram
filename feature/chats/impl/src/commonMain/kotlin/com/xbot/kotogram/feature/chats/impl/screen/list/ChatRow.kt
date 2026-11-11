package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.xbot.kotogram.core.designsystem.component.ArchivedChat
import com.xbot.kotogram.core.designsystem.component.ArchivedChats
import com.xbot.kotogram.core.designsystem.component.Avatar
import com.xbot.kotogram.core.designsystem.component.AvatarBadge
import com.xbot.kotogram.core.designsystem.component.AvatarIcon
import com.xbot.kotogram.core.designsystem.component.Badges
import com.xbot.kotogram.core.designsystem.component.ChatAvatar
import com.xbot.kotogram.core.designsystem.component.ChatListItem
import com.xbot.kotogram.core.designsystem.component.ChatListItemScope
import com.xbot.kotogram.core.designsystem.component.ChatListItemSeparator
import com.xbot.kotogram.core.designsystem.component.Date
import com.xbot.kotogram.core.designsystem.component.Draft
import com.xbot.kotogram.core.designsystem.component.FolderTag
import com.xbot.kotogram.core.designsystem.component.ForumTopic
import com.xbot.kotogram.core.designsystem.component.Message
import com.xbot.kotogram.core.designsystem.component.MessageThumbnail
import com.xbot.kotogram.core.designsystem.component.NetworkImage
import com.xbot.kotogram.core.designsystem.component.OpenAppButton
import com.xbot.kotogram.core.designsystem.component.SPOILER_ANNOTATION
import com.xbot.kotogram.core.designsystem.component.StoryRing
import com.xbot.kotogram.core.designsystem.component.Title
import com.xbot.kotogram.core.designsystem.component.TitleMark
import com.xbot.kotogram.core.designsystem.component.TopicIcon
import com.xbot.kotogram.core.designsystem.component.Typing
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.StringResource
import com.xbot.kotogram.core.resources.chatlist_archivedchats
import com.xbot.kotogram.core.resources.chatlist_draft
import com.xbot.kotogram.core.resources.chatlist_monoforum_holder
import com.xbot.kotogram.core.resources.chatlist_openminiapp
import com.xbot.kotogram.core.resources.chatlist_you
import com.xbot.kotogram.core.resources.gallery_pagecontroller_live
import com.xbot.kotogram.core.resources.mark_fake
import com.xbot.kotogram.core.resources.mark_scam
import com.xbot.kotogram.core.resources.messagetimer_shortdays
import com.xbot.kotogram.core.resources.messagetimer_shorthours
import com.xbot.kotogram.core.resources.messagetimer_shortminutes
import com.xbot.kotogram.core.resources.messagetimer_shortmonths
import com.xbot.kotogram.core.resources.messagetimer_shortseconds
import com.xbot.kotogram.core.resources.messagetimer_shortweeks
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
import com.xbot.kotogram.core.resources.peer_deleteduser
import com.xbot.kotogram.core.resources.peer_repliesnotifications
import com.xbot.kotogram.core.resources.peer_savedmessages
import com.xbot.kotogram.core.resources.relative
import com.xbot.kotogram.core.resources.relativeDateString
import com.xbot.kotogram.core.resources.rememberDateFormats
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.chats.domain.model.ArchiveSummary
import com.xbot.kotogram.feature.chats.impl.component.CustomEmoji
import com.xbot.kotogram.feature.chats.impl.component.customEmojiContent
import com.xbot.kotogram.feature.chats.impl.component.toPreviewString
import com.xbot.kotogram.feature.chats.impl.extensions.resolved
import com.xbot.kotogram.feature.chats.impl.extensions.text
import com.xbot.kotogram.feature.chats.impl.extensions.toBadge
import com.xbot.kotogram.feature.chats.impl.extensions.toMark
import com.xbot.kotogram.feature.chats.impl.extensions.toRing
import com.xbot.kotogram.feature.chats.impl.extensions.toTopic
import io.xbot.tdlib.api.Sticker
import org.jetbrains.compose.resources.StringResource as ComposeStringResource

@Composable
context(style: ChatListScreenStyleScope)
internal fun ChatRow(
    row: ChatListRow,
    customEmoji: Map<Long, Sticker>,
    separator: ChatListItemSeparator,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emojiSize = with(LocalDensity.current) { style.previewEmojiSize.toSp() }
    val inlineContent = customEmojiContent(customEmoji, row.customEmojiIds, emojiSize)
    val tags = row.tags.map { FolderTag(it.title, it.colorId) }
    ChatListItem(
        avatar = {
            ChatAvatar(stories = row.stories?.toRing(), badge = row.avatarBadge?.toBadge(), shape = row.avatar.shape) {
                val icon = row.avatar.icon
                if (icon != null) {
                    Avatar(icon, Modifier.fillMaxSize(), shape = row.avatar.shape)
                } else {
                    Avatar(
                        photo = row.avatar.photo,
                        letters = row.avatar.letters,
                        colorIndex = row.avatar.colorIndex,
                        thumbnail = row.avatar.thumbnail,
                        shape = row.avatar.shape,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        },
        title = {
            Title(
                text = row.title.text(),
                mark = row.mark?.toMark(),
                muted = row.isMuted,
                secret = row.isSecret,
                label = if (row.isDirectMessages) stringResource(Res.string.chatlist_monoforum_holder) else null,
                emojiStatus = row.emojiStatusId?.let { id ->
                    @Composable { CustomEmoji(customEmoji[id], "", Modifier.fillMaxSize(), loops = EMOJI_STATUS_LOOPS) }
                },
                botVerification = row.botVerificationEmojiId?.let { id ->
                    @Composable { CustomEmoji(customEmoji[id], "", Modifier.fillMaxSize(), loops = 0) }
                },
            )
        },
        date = if (row.date != 0) {
            { Date(relativeDateString(row.date.toLong()), status = row.status) }
        } else {
            null
        },
        text = {
            Crossfade(targetState = row.typing != null, animationSpec = tween(TYPING_FADE_MILLIS)) { typing ->
                RowText(row, typing, tags, inlineContent, customEmoji)
            }
        },
        badges = {
            if (row.hasOpenAppButton) {
                OpenAppButton(stringResource(Res.string.chatlist_openminiapp), onClick = onClick)
            } else {
                Badges(
                    unreadCount = when {
                        row.unreadCount > 0 -> compactCount(row.unreadCount)
                        row.isMarkedAsUnread -> ""
                        else -> null
                    },
                    muted = row.isMuted,
                    mention = row.hasUnreadMention,
                    reaction = row.hasUnreadReaction,
                    pollVotes = row.hasUnreadPollVotes,
                    pinned = row.isPinned,
                )
            }
        },
        pinned = row.isPinned,
        separator = separator,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun ChatListItemScope.RowText(
    row: ChatListRow,
    typing: Boolean,
    tags: List<FolderTag>,
    inlineContent: Map<String, InlineTextContent>,
    customEmoji: Map<Long, Sticker>,
) {
    val activity = row.typing
    when {
        typing && activity != null -> Typing(activity.text(), activity.action)

        row.preview is ChatPreview.Draft -> Draft(
            label = draftLabel(),
            text = row.preview.text.toPreviewString(),
            tags = tags,
            inlineContent = inlineContent,
        )

        row.preview is ChatPreview.MediaDraft -> Draft(
            label = draftLabel(),
            text = AnnotatedString(row.preview.label.resolved()),
            tags = tags,
        )

        else -> Message(
            text = previewText(row.preview),
            author = row.author?.text(),
            icon = (row.preview as? ChatPreview.Message)?.icon,
            thumbnails = previewThumbnails(row.preview),
            topics = row.topics.map { it.toTopic(customEmoji) },
            tags = tags,
            inlineContent = inlineContent,
        )
    }
}

@Composable
internal fun ArchiveRow(
    summary: ArchiveSummary,
    separator: ChatListItemSeparator,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChatListItem(
        avatar = { Avatar(AvatarIcon.Archive, Modifier.fillMaxSize()) },
        title = { Title(stringResource(Res.string.chatlist_archivedchats)) },
        text = {
            ArchivedChats(
                summary.chats.map {
                    ArchivedChat(it.title, unread = it.unreadCount > 0 || it.isMarkedAsUnread)
                },
            )
        },
        badges = {
            Badges(
                unreadCount = if (summary.unreadCount > 0) compactCount(summary.unreadCount) else null,
                muted = !summary.hasUnmutedUnread,
            )
        },
        pinned = true,
        separator = separator,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun draftLabel(): String = stringResource(Res.string.chatlist_draft).trimEnd(':')

@Composable
private fun previewText(preview: ChatPreview): AnnotatedString = when (preview) {
    is ChatPreview.Message -> when (val content = preview.content) {
        is PreviewText.Formatted -> {
            val text = content.text.toPreviewString()
            if (content.hiddenCode) text.withHiddenCode() else text
        }

        is PreviewText.Plain -> AnnotatedString(content.text)

        is PreviewText.Resource -> AnnotatedString(content.resource.resolved())
    }

    else -> AnnotatedString("")
}

private fun AnnotatedString.withHiddenCode(): AnnotatedString {
    val match = CodePattern.find(text) ?: return this
    return buildAnnotatedString {
        append(this@withHiddenCode)
        addStringAnnotation(SPOILER_ANNOTATION, "", match.range.first, match.range.last + 1)
    }
}

private fun previewThumbnails(preview: ChatPreview): List<MessageThumbnail> {
    if (preview !is ChatPreview.Message) return emptyList()
    return preview.thumbnails.map { thumbnail ->
        MessageThumbnail(
            image = {
                NetworkImage(model = thumbnail.image, contentDescription = null, modifier = Modifier.fillMaxSize())
            },
            type = thumbnail.type,
            spoiler = thumbnail.spoiler,
        )
    }
}

private fun compactCount(count: Int): String = when {
    count < THOUSAND -> count.toString()
    count < MILLION -> tenths(count, THOUSAND) + "K"
    else -> tenths(count, MILLION) + "M"
}

private fun tenths(
    count: Int,
    unit: Int,
): String {
    val tenths = count / (unit / TEN)
    return if (tenths % TEN == 0) "${tenths / TEN}" else "${tenths / TEN}.${tenths % TEN}"
}

private val CodePattern = Regex("\\b\\d{5,8}\\b")
private const val THOUSAND = 1_000
private const val MILLION = 1_000_000
private const val TEN = 10
private const val TYPING_FADE_MILLIS = 150
private const val EMOJI_STATUS_LOOPS = 2

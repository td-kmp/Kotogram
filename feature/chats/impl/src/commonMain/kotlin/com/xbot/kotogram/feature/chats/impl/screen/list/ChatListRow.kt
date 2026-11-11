package com.xbot.kotogram.feature.chats.impl.screen.list

import com.xbot.kotogram.core.designsystem.component.AvatarIcon
import com.xbot.kotogram.core.designsystem.component.AvatarShape
import com.xbot.kotogram.core.designsystem.component.MessageIcon
import com.xbot.kotogram.core.designsystem.component.MessageStatus
import com.xbot.kotogram.core.designsystem.component.MessageThumbnailType
import com.xbot.kotogram.core.designsystem.component.TypingAction
import com.xbot.kotogram.core.resources.StringResource
import com.xbot.kotogram.feature.chats.impl.extensions.customEmojiIds
import io.xbot.tdlib.api.File
import io.xbot.tdlib.api.FormattedText

internal class ChatListRow(
    val chatId: Long,
    val title: RowTitle,
    val avatar: AvatarModel,
    val stories: StoriesModel?,
    val avatarBadge: AvatarBadgeModel?,
    val mark: TitleMarkModel?,
    val emojiStatusId: Long?,
    val botVerificationEmojiId: Long?,
    val isSecret: Boolean,
    val isDirectMessages: Boolean,
    val isMuted: Boolean,
    val isPinned: Boolean,
    val date: Int,
    val status: MessageStatus?,
    val unreadCount: Int,
    val hasUnreadMention: Boolean,
    val hasUnreadReaction: Boolean,
    val hasUnreadPollVotes: Boolean,
    val isMarkedAsUnread: Boolean,
    val author: AuthorName?,
    val preview: ChatPreview,
    val typing: TypingModel?,
    val topics: List<TopicModel>,
    val tags: List<TagModel>,
    val hasOpenAppButton: Boolean,
) {
    val customEmojiIds: Set<Long> = buildSet {
        emojiStatusId?.let(::add)
        botVerificationEmojiId?.let(::add)
        preview.text?.customEmojiIds()?.let(::addAll)
        topics.forEach { topic -> (topic.icon as? TopicIconModel.Emoji)?.let { add(it.customEmojiId) } }
    }
}

internal sealed interface RowTitle {
    class Name(
        val text: String,
    ) : RowTitle

    data object SavedMessages : RowTitle

    data object Replies : RowTitle

    data object DeletedAccount : RowTitle
}

internal class AvatarModel(
    val photo: File?,
    val thumbnail: ByteArray?,
    val letters: String,
    val colorIndex: Int,
    val icon: AvatarIcon? = null,
    val shape: AvatarShape = AvatarShape.Circle,
)

internal sealed interface StoriesModel {
    class Stories(
        val total: Int,
        val unseen: Int,
        val closeFriends: Boolean,
    ) : StoriesModel

    data object Live : StoriesModel
}

internal sealed interface AvatarBadgeModel {
    data object Online : AvatarBadgeModel

    data object VoiceChat : AvatarBadgeModel

    class AutoDelete(
        val seconds: Int,
    ) : AvatarBadgeModel

    data object Subscription : AvatarBadgeModel
}

internal enum class TitleMarkModel { Verified, Premium, Scam, Fake }

internal sealed interface AuthorName {
    class Name(
        val text: String,
    ) : AuthorName

    data object You : AuthorName
}

internal sealed interface ChatPreview {
    val text: FormattedText? get() = null

    data object Empty : ChatPreview

    class Draft(
        override val text: FormattedText,
    ) : ChatPreview

    class MediaDraft(
        val label: StringResource,
    ) : ChatPreview

    class Message(
        val content: PreviewText,
        val icon: MessageIcon?,
        val thumbnails: List<ThumbnailModel>,
        val hideAuthor: Boolean,
    ) : ChatPreview {
        override val text: FormattedText? get() = (content as? PreviewText.Formatted)?.text
    }
}

internal sealed interface PreviewText {
    class Formatted(
        val text: FormattedText,
        val hiddenCode: Boolean = false,
    ) : PreviewText

    class Plain(
        val text: String,
    ) : PreviewText

    class Resource(
        val resource: StringResource,
    ) : PreviewText
}

internal class ThumbnailModel(
    val image: Any?,
    val type: MessageThumbnailType,
    val spoiler: Boolean,
)

internal class TypingModel(
    val names: List<String>,
    val action: TypingAction,
    val kind: TypingKind,
    val inPrivateChat: Boolean,
)

internal enum class TypingKind { Text, Voice, Video, Photo, VideoUpload, File, Game, Sticker }

internal class TopicModel(
    val title: String,
    val icon: TopicIconModel?,
    val unread: Boolean,
)

internal sealed interface TopicIconModel {
    data object General : TopicIconModel

    class Bubble(
        val color: Int,
    ) : TopicIconModel

    class Emoji(
        val customEmojiId: Long,
    ) : TopicIconModel
}

internal class TagModel(
    val title: String,
    val colorId: Int,
)

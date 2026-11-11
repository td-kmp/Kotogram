package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.component.ArchivedChat
import com.xbot.kotogram.core.designsystem.component.ArchivedChats
import com.xbot.kotogram.core.designsystem.component.Avatar
import com.xbot.kotogram.core.designsystem.component.AvatarBadge
import com.xbot.kotogram.core.designsystem.component.AvatarIcon
import com.xbot.kotogram.core.designsystem.component.AvatarShape
import com.xbot.kotogram.core.designsystem.component.Badges
import com.xbot.kotogram.core.designsystem.component.ChatAvatar
import com.xbot.kotogram.core.designsystem.component.ChatListItem
import com.xbot.kotogram.core.designsystem.component.ChatListItemSeparator
import com.xbot.kotogram.core.designsystem.component.Date
import com.xbot.kotogram.core.designsystem.component.Draft
import com.xbot.kotogram.core.designsystem.component.FolderTag
import com.xbot.kotogram.core.designsystem.component.ForumTopic
import com.xbot.kotogram.core.designsystem.component.Message
import com.xbot.kotogram.core.designsystem.component.MessageIcon
import com.xbot.kotogram.core.designsystem.component.MessageStatus
import com.xbot.kotogram.core.designsystem.component.MessageThumbnail
import com.xbot.kotogram.core.designsystem.component.MessageThumbnailType
import com.xbot.kotogram.core.designsystem.component.OpenAppButton
import com.xbot.kotogram.core.designsystem.component.SPOILER_ANNOTATION
import com.xbot.kotogram.core.designsystem.component.StoryRing
import com.xbot.kotogram.core.designsystem.component.Title
import com.xbot.kotogram.core.designsystem.component.TitleMark
import com.xbot.kotogram.core.designsystem.component.TopicIcon
import com.xbot.kotogram.core.designsystem.component.Typing
import com.xbot.kotogram.core.designsystem.component.TypingAction

internal val ChatListItemPage = CatalogPage(
    id = "chat-list-item",
    section = CatalogSection.Components,
    name = "ChatListItem",
    description = "A row of the chat list after Telegram for iOS's ChatListItem: the avatar with its story ring " +
        "and badges, the title with its marks, the date with the outgoing status, the preview (author, topics, " +
        "tags, typing) and the badges at the bottom right.",
    examples = listOf(
        CatalogExample("Private chats") {
            Column {
                SampleChat("Alice Carter", "18:19", "See you tomorrow at the station", colorIndex = 0)
                SampleChat(
                    title = "Alice Carter",
                    date = "13/10",
                    text = "Boarding pass 29-06-26.pdf",
                    colorIndex = 0,
                    icon = MessageIcon.Forwarded,
                    pinned = true,
                    separator = ChatListItemSeparator.FullWidth,
                )
                SampleChat("Bob Miller", "Mon", "I will bring the slides", colorIndex = 2, status = MessageStatus.Read)
                SampleChat("Carol White", "12:05", "On my way", colorIndex = 3, status = MessageStatus.Sent)
                SampleChat("Dan Brown", "12:04", "Uploading the photos", colorIndex = 4, status = MessageStatus.Sending)
                SampleChat("Eve Black", "12:01", "It did not go through", colorIndex = 5, status = MessageStatus.Failed)
                SampleChat(
                    title = "Frank Moss",
                    date = "11:58",
                    text = "Reacted 👍 to your message",
                    colorIndex = 1,
                    reaction = true,
                    badge = AvatarBadge.Online,
                )
            }
        },
        CatalogExample("Avatars and stories") {
            Column {
                SampleChat("Grace Hall", "10:10", "One new story", colorIndex = 1, stories = StoryRing.Stories(1, 1))
                SampleChat("Hugo Hart", "10:05", "Two of three stories unseen", 2, stories = StoryRing.Stories(3, 2))
                SampleChat(
                    "Iris Kent",
                    "10:01",
                    "Close friends",
                    3,
                    stories = StoryRing.Stories(1, 1, closeFriends = true),
                )
                SampleChat("Jack Lane", "09:58", "Stories seen", 4, stories = StoryRing.Stories(4, 0))
                SampleChat("Kate Long", "09:55", "Live now", 5, stories = StoryRing.Live("LIVE"))
                SampleChat(
                    title = "Kotlin Forum",
                    date = "09:50",
                    text = "A forum's avatar is a rounded square",
                    colorIndex = 6,
                    stories = StoryRing.Stories(2, 1),
                    shape = AvatarShape.RoundedRect,
                )
                SampleChat("Liam Moore", "09:45", "Disappears in a day", 0, badge = AvatarBadge.AutoDelete("1d"))
                SampleChat(
                    "Night shift",
                    "09:40",
                    "A video chat is on",
                    2,
                    author = "Mia",
                    badge = AvatarBadge.VoiceChat,
                )
                SampleChat("Premium club", "09:35", "Paid subscription", 4, badge = AvatarBadge.Subscription)
                IconChat(AvatarIcon.SavedMessages, "Saved Messages", "Boarding pass 29-06-26.pdf")
                IconChat(AvatarIcon.Replies, "Replies", "Thanks for the answer!", author = "Nora")
                IconChat(AvatarIcon.HiddenAuthor, "Author hidden", "Forwarded from a hidden account")
                IconChat(AvatarIcon.Deleted, "Deleted Account", "Hi there")
            }
        },
        CatalogExample("Title marks") {
            Column {
                SampleChat("Kotlin Weekly", "20:26", "Verified", colorIndex = 5, mark = TitleMark.Verified)
                SampleChat("Olivia Park", "20:25", "Telegram Premium", colorIndex = 6, mark = TitleMark.Premium)
                SampleChat("Paul Reed", "20:24", "An emoji status", colorIndex = 0, emojiStatus = true)
                SampleChat(
                    "Free Crypto",
                    "20:23",
                    "Send 1 coin to get 2",
                    colorIndex = 1,
                    mark = TitleMark.Warning("SCAM"),
                )
                SampleChat(
                    "Kotlin Weekly",
                    "20:22",
                    "Not the real one",
                    colorIndex = 2,
                    mark = TitleMark.Warning("FAKE"),
                )
                SampleChat("Quinn Ross", "20:21", "End-to-end encrypted", colorIndex = 3, secret = true)
                SampleChat("Study Bot", "20:20", "Verified by an organisation", 4, botVerification = true)
                SampleChat("Kotlin Weekly", "20:19", "Messages to the channel", colorIndex = 5, label = "DIRECT")
                SampleChat(
                    title = "A very long name that does not fit in a single line of the row",
                    date = "09:30",
                    text = "The icons stay after the ellipsis",
                    colorIndex = 0,
                    mark = TitleMark.Verified,
                    emojiStatus = true,
                    muted = true,
                    unread = "1",
                )
            }
        },
        CatalogExample("Groups and channels") {
            Column {
                SampleChat(
                    title = "Neighbourhood council of the northern district",
                    date = "20:27",
                    author = "Eco Watch",
                    text = "Thanks for the report, it has been passed on!",
                    colorIndex = 1,
                    muted = true,
                    unread = "6.6K",
                )
                SampleChat(
                    title = "Kotlin Weekly",
                    date = "20:26",
                    text = "Compose Multiplatform 1.12 is out with a new text field and shared element transitions",
                    colorIndex = 5,
                    mark = TitleMark.Verified,
                    unread = "12",
                    thumbnails = 3,
                )
                SampleChat(
                    "Design review",
                    "20:20",
                    "@you could you look at the chat list?",
                    6,
                    author = "Mia",
                    unread = "3",
                    mention = true,
                )
                SampleChat(
                    "Weekend trip",
                    "19:58",
                    "Booked the cabin",
                    2,
                    author = "Leo",
                    reaction = true,
                    muted = true,
                )
                SampleChat(
                    "Team lunch",
                    "19:50",
                    "Where shall we go?",
                    3,
                    author = "You",
                    status = MessageStatus.Read,
                    pollVotes = true,
                )
            }
        },
        CatalogExample("Media and message types") {
            Column {
                SampleChat(
                    "Ann Lee",
                    "21:22",
                    "Voice message",
                    3,
                    author = "Max",
                    icon = MessageIcon.VoiceMessage,
                    unread = "2",
                )
                SampleChat(
                    "Ann Lee",
                    "21:22",
                    "Video message",
                    3,
                    thumbnails = 1,
                    thumbnailType = MessageThumbnailType.RoundVideo,
                )
                SampleChat("Photos", "21:17", "Album", colorIndex = 1, thumbnails = 3, muted = true, unread = "171")
                SampleChat(
                    "Trailers",
                    "21:15",
                    "The new season starts on Friday",
                    4,
                    thumbnails = 1,
                    thumbnailType = MessageThumbnailType.Video,
                )
                SampleChat("Secrets", "21:14", "Photo", colorIndex = 0, thumbnails = 1, spoilerMedia = true)
                SampleChat("Puzzles", "21:13", "", colorIndex = 2, spoilerText = "The answer is" to "forty-two")
                SampleChat(
                    "Ann Lee",
                    "21:24",
                    "Location",
                    3,
                    author = "You",
                    status = MessageStatus.Sent,
                    icon = MessageIcon.Location,
                )
                SampleChat(
                    "Ann Lee",
                    "21:24",
                    "Lunch?",
                    3,
                    author = "You",
                    status = MessageStatus.Sent,
                    icon = MessageIcon.Poll,
                )
                SampleChat("Ann Lee", "21:23", "Groceries", 3, icon = MessageIcon.Todo)
                SampleChat("Game night", "21:21", "Lumberjack", 4, icon = MessageIcon.Game)
                SampleChat("Ben Ward", "21:20", "Nice view!", 5, icon = MessageIcon.StoryReply)
                SampleChat("Ben Ward", "21:19", "Gift", 5, icon = MessageIcon.Gift)
                SampleChat(
                    "Max",
                    "21:23",
                    "Missed call",
                    2,
                    icon = MessageIcon.IncomingCall,
                    muted = true,
                    unread = "1",
                )
                SampleChat("Max", "21:20", "Outgoing call (2 min)", 2, icon = MessageIcon.OutgoingCall)
                SampleChat("Max", "21:18", "Incoming video call", 2, icon = MessageIcon.IncomingVideoCall)
                SampleChat("Max", "21:17", "Outgoing video call", 2, icon = MessageIcon.OutgoingVideoCall)
                SampleChat("Radio", "21:19", "Song of the day", colorIndex = 5, icon = MessageIcon.Audio)
            }
        },
        CatalogExample("Typing") {
            Column {
                TypingChat("Alice Carter", 0, "typing", TypingAction.Typing)
                TypingChat("Alice Carter", 0, "recording a voice message", TypingAction.RecordingVoice)
                TypingChat("Alice Carter", 0, "recording a video message", TypingAction.RecordingVideo)
                TypingChat("Alice Carter", 0, "sending a photo", TypingAction.Uploading)
                TypingChat("Game night", 4, "Max is playing a game", TypingAction.PlayingGame)
                TypingChat("Alice Carter", 0, "choosing a sticker", TypingAction.ChoosingSticker)
                TypingChat("Design review", 6, "Mia and Leo are typing", TypingAction.Typing)
            }
        },
        CatalogExample("Forums") {
            Column {
                ForumChat(
                    topics = listOf(
                        ForumTopic("Releases", TopicIcon.Bubble(0x6FB9F0), unread = true),
                        ForumTopic("General", TopicIcon.General),
                        ForumTopic("Off-topic", TopicIcon.Bubble(0xFF93B2)),
                    ),
                    author = "Mia",
                    text = "1.12 is out, the notes are in the pinned message",
                    unread = "5",
                )
                ForumChat(
                    topics = listOf(
                        ForumTopic("Design", TopicIcon.Bubble(0xFFD67E)),
                        ForumTopic("Bugs", TopicIcon.Emoji { SampleEmojiStatus() }),
                    ),
                    author = "You",
                    text = "Pushed the fix",
                    status = MessageStatus.Read,
                )
            }
        },
        CatalogExample("Folder tags") {
            Column {
                SampleChat(
                    title = "Design review",
                    date = "18:02",
                    text = "Could you look at the chat list?",
                    colorIndex = 6,
                    author = "Mia",
                    tags = listOf(FolderTag("Work", 5), FolderTag("Design", 2)),
                )
                SampleChat(
                    title = "Alice Carter",
                    date = "17:40",
                    text = "See you tomorrow",
                    colorIndex = 0,
                    tags = listOf(
                        FolderTag("Family", 3),
                        FolderTag("Friends", 1),
                        FolderTag("Close", 6),
                        FolderTag("All", 0),
                    ),
                )
                ChatListItem(
                    avatar = { SampleAvatar("GH", colorIndex = 3) },
                    title = { Title("Grace Hopper") },
                    date = { Date("11:42") },
                    text = {
                        Draft(
                            "Draft",
                            AnnotatedString("Let me check the compiler first"),
                            tags = listOf(FolderTag("Work", 5)),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )
            }
        },
        CatalogExample("Drafts, unread marks and pins") {
            Column {
                ChatListItem(
                    avatar = { SampleAvatar("GH", colorIndex = 3) },
                    title = { Title("Grace Hopper") },
                    date = { Date("11:42") },
                    text = { Draft("Draft", AnnotatedString("Let me check the compiler first")) },
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )
                ChatListItem(
                    avatar = { SampleAvatar("GH", colorIndex = 3) },
                    title = { Title("Grace Hopper") },
                    date = { Date("11:40") },
                    text = { Draft("Draft", AnnotatedString("Voice message")) },
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )
                SampleChat("Henry Ford", "Sun", "Call me when you are free", colorIndex = 4, unread = "")
                SampleChat("Ivy Chen", "Sat", "Thanks!", colorIndex = 6, pinned = true)
            }
        },
        CatalogExample("Archive and bots") {
            Column {
                ArchiveChat(hidden = false)
                ArchiveChat(hidden = true)
                ChatListItem(
                    avatar = { SampleAvatar("W", colorIndex = 5) },
                    title = { Title("Weather Bot") },
                    date = { Date("08:00") },
                    text = { Message(AnnotatedString("Sunny, 24°")) },
                    badges = { OpenAppButton("Open", onClick = {}) },
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )
            }
        },
    ),
)

@Composable
@Suppress("LongParameterList", "LongMethod")
private fun SampleChat(
    title: String,
    date: String,
    text: String,
    colorIndex: Int,
    author: String? = null,
    status: MessageStatus? = null,
    unread: String? = null,
    muted: Boolean = false,
    mark: TitleMark? = null,
    emojiStatus: Boolean = false,
    secret: Boolean = false,
    botVerification: Boolean = false,
    label: String? = null,
    mention: Boolean = false,
    reaction: Boolean = false,
    pollVotes: Boolean = false,
    icon: MessageIcon? = null,
    pinned: Boolean = false,
    badge: AvatarBadge? = null,
    stories: StoryRing? = null,
    shape: AvatarShape = AvatarShape.Circle,
    thumbnails: Int = 0,
    thumbnailType: MessageThumbnailType = MessageThumbnailType.Photo,
    spoilerMedia: Boolean = false,
    spoilerText: Pair<String, String>? = null,
    tags: List<FolderTag> = emptyList(),
    separator: ChatListItemSeparator = ChatListItemSeparator.Inset,
) {
    val letters = title.split(' ').take(2).joinToString("") { it.take(1) }
    ChatListItem(
        avatar = {
            ChatAvatar(stories = stories, badge = badge, shape = shape) {
                Avatar(null, letters, colorIndex, Modifier.fillMaxSize(), shape = shape)
            }
        },
        title = {
            Title(
                text = title,
                mark = mark,
                muted = muted,
                secret = secret,
                label = label,
                emojiStatus = if (emojiStatus) ({ SampleEmojiStatus() }) else null,
                botVerification = if (botVerification) ({ SampleEmojiStatus(Color(0xFF2E7DF6)) }) else null,
            )
        },
        date = { Date(date, status = status) },
        text = {
            Message(
                text = spoilerText?.let(::spoiler) ?: AnnotatedString(text),
                author = author,
                icon = icon,
                thumbnails = List(thumbnails) { index ->
                    MessageThumbnail({ SampleThumbnail(index) }, thumbnailType, spoiler = spoilerMedia)
                },
                tags = tags,
            )
        },
        badges = {
            Badges(
                unreadCount = unread,
                muted = muted,
                mention = mention,
                reaction = reaction,
                pollVotes = pollVotes,
                pinned = pinned,
            )
        },
        pinned = pinned,
        separator = separator,
        modifier = Modifier.fillMaxWidth(),
        onClick = {},
    )
}

private fun spoiler(parts: Pair<String, String>): AnnotatedString = buildAnnotatedString {
    append(parts.first)
    append(' ')
    val start = length
    append(parts.second)
    addStringAnnotation(SPOILER_ANNOTATION, "", start, length)
}

@Composable
private fun IconChat(
    icon: AvatarIcon,
    title: String,
    text: String,
    author: String? = null,
) {
    ChatListItem(
        avatar = { Avatar(icon, Modifier.fillMaxSize()) },
        title = { Title(title) },
        date = { Date("Tue") },
        text = { Message(AnnotatedString(text), author = author) },
        modifier = Modifier.fillMaxWidth(),
        onClick = {},
    )
}

@Composable
private fun TypingChat(
    title: String,
    colorIndex: Int,
    text: String,
    action: TypingAction,
) {
    val letters = title.split(' ').take(2).joinToString("") { it.take(1) }
    ChatListItem(
        avatar = { SampleAvatar(letters, colorIndex) },
        title = { Title(title) },
        date = { Date("12:00") },
        text = { Typing(text, action) },
        modifier = Modifier.fillMaxWidth(),
        onClick = {},
    )
}

@Composable
private fun ForumChat(
    topics: List<ForumTopic>,
    author: String,
    text: String,
    unread: String? = null,
    status: MessageStatus? = null,
) {
    ChatListItem(
        avatar = {
            ChatAvatar(shape = AvatarShape.RoundedRect) {
                Avatar(null, "K", 5, Modifier.fillMaxSize(), shape = AvatarShape.RoundedRect)
            }
        },
        title = { Title("Kotlin Forum") },
        date = { Date("16:20", status = status) },
        text = { Message(AnnotatedString(text), author = author, topics = topics) },
        badges = { Badges(unreadCount = unread) },
        modifier = Modifier.fillMaxWidth(),
        onClick = {},
    )
}

@Composable
private fun ArchiveChat(hidden: Boolean) {
    ChatListItem(
        avatar = { Avatar(if (hidden) AvatarIcon.HiddenArchive else AvatarIcon.Archive, Modifier.fillMaxSize()) },
        title = { Title("Archived Chats") },
        text = {
            ArchivedChats(
                listOf(
                    ArchivedChat("Old classmates", unread = true),
                    ArchivedChat("Flat 12"),
                    ArchivedChat("Delivery Bot", unread = true),
                    ArchivedChat("Kotlin Weekly"),
                ),
            )
        },
        badges = { Badges(unreadCount = "4", muted = true) },
        pinned = !hidden,
        modifier = Modifier.fillMaxWidth(),
        onClick = {},
    )
}

@Composable
private fun SampleThumbnail(index: Int) {
    val colors = listOf(Color(0xFF7FB3D5), Color(0xFFF5B041), Color(0xFF82E0AA))
    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(3f)).background(colors[index % colors.size]))
}

@Composable
private fun SampleAvatar(
    letters: String,
    colorIndex: Int,
) {
    Avatar(photo = null, letters = letters, colorIndex = colorIndex, modifier = Modifier.fillMaxSize())
}

@Composable
private fun SampleEmojiStatus(color: Color = Color(0xFFFFB300)) {
    Box(Modifier.fillMaxSize().clip(CircleShape).background(color))
}

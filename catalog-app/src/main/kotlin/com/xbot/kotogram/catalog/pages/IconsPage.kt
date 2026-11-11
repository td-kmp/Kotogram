package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons

private val Common = listOf(
    "Back" to KotogramIcons.Back,
    "Close" to KotogramIcons.Close,
    "Cross" to KotogramIcons.Cross,
    "Clear" to KotogramIcons.Clear,
    "Search" to KotogramIcons.Search,
    "ChevronDown" to KotogramIcons.ChevronDown,
    "ChevronRight" to KotogramIcons.ChevronRight,
    "More" to KotogramIcons.More,
    "Check" to KotogramIcons.Check,
    "DoubleCheck" to KotogramIcons.DoubleCheck,
)

private val SignIn = listOf(
    "Eye" to KotogramIcons.Eye,
    "EyeOff" to KotogramIcons.EyeOff,
    "QrCode" to KotogramIcons.QrCode,
    "Smartphone" to KotogramIcons.Smartphone,
)

private val ChatList = listOf(
    "Compose" to KotogramIcons.Compose,
    "Edit" to KotogramIcons.Edit,
    "Pin" to KotogramIcons.Pin,
    "Bell" to KotogramIcons.Bell,
    "BellOff" to KotogramIcons.BellOff,
    "Clock" to KotogramIcons.Clock,
    "Lock" to KotogramIcons.Lock,
    "Archive" to KotogramIcons.Archive,
    "Folder" to KotogramIcons.Folder,
    "Bookmark" to KotogramIcons.Bookmark,
)

private val Chat = listOf(
    "Attach" to KotogramIcons.Attach,
    "Send" to KotogramIcons.Send,
    "Microphone" to KotogramIcons.Microphone,
    "Smile" to KotogramIcons.Smile,
    "Reply" to KotogramIcons.Reply,
    "Forward" to KotogramIcons.Forward,
    "Copy" to KotogramIcons.Copy,
    "Delete" to KotogramIcons.Delete,
    "ArrowDown" to KotogramIcons.ArrowDown,
    "Call" to KotogramIcons.Call,
    "Video" to KotogramIcons.Video,
    "Info" to KotogramIcons.Info,
    "Camera" to KotogramIcons.Camera,
)

private val SettingsAndProfile = listOf(
    "Settings" to KotogramIcons.Settings,
    "Person" to KotogramIcons.Person,
    "PersonAdd" to KotogramIcons.PersonAdd,
    "AtSign" to KotogramIcons.AtSign,
    "Globe" to KotogramIcons.Globe,
    "Appearance" to KotogramIcons.Appearance,
    "Storage" to KotogramIcons.Storage,
    "Devices" to KotogramIcons.Devices,
    "Help" to KotogramIcons.Help,
    "Logout" to KotogramIcons.Logout,
    "Share" to KotogramIcons.Share,
    "Block" to KotogramIcons.Block,
    "Shield" to KotogramIcons.Shield,
    "Plus" to KotogramIcons.Plus,
    "Message" to KotogramIcons.Message,
)

private val ChatListRow = listOf(
    "SpeakerOff" to KotogramIcons.SpeakerOff,
    "PinFilled" to KotogramIcons.PinFilled,
    "ForwardFilled" to KotogramIcons.ForwardFilled,
    "MentionBadge" to KotogramIcons.MentionBadge,
    "ReactionBadge" to KotogramIcons.ReactionBadge,
    "VerifiedBadge" to KotogramIcons.VerifiedBadge,
    "VerifiedCheck" to KotogramIcons.VerifiedCheck,
    "MessageVoice" to KotogramIcons.MessageVoice,
    "MessageAudio" to KotogramIcons.MessageAudio,
    "MessageLocation" to KotogramIcons.MessageLocation,
    "MessagePoll" to KotogramIcons.MessagePoll,
    "MessageCallIncoming" to KotogramIcons.MessageCallIncoming,
    "MessageCallOutgoing" to KotogramIcons.MessageCallOutgoing,
    "ThumbnailPlay" to KotogramIcons.ThumbnailPlay,
    "MessageVideoCallIncoming" to KotogramIcons.MessageVideoCallIncoming,
    "MessageVideoCallOutgoing" to KotogramIcons.MessageVideoCallOutgoing,
    "MessageStoryReply" to KotogramIcons.MessageStoryReply,
    "MessageGift" to KotogramIcons.MessageGift,
    "MessageTodo" to KotogramIcons.MessageTodo,
    "MessageGame" to KotogramIcons.MessageGame,
    "PollVotesBadge" to KotogramIcons.PollVotesBadge,
    "PremiumStar" to KotogramIcons.PremiumStar,
    "SecretLock" to KotogramIcons.SecretLock,
    "GeneralTopic" to KotogramIcons.GeneralTopic,
    "TopicArrow" to KotogramIcons.TopicArrow,
    "TopicClosed" to KotogramIcons.TopicClosed,
)

private val Avatars = listOf(
    "AvatarSavedMessages" to KotogramIcons.AvatarSavedMessages,
    "AvatarReplies" to KotogramIcons.AvatarReplies,
    "AvatarMyNotes" to KotogramIcons.AvatarMyNotes,
    "AvatarAnonymous" to KotogramIcons.AvatarAnonymous,
    "AvatarArchive" to KotogramIcons.AvatarArchive,
    "AvatarDeleted" to KotogramIcons.AvatarDeleted,
)

private val AllIcons = Common + SignIn + ChatList + Chat + SettingsAndProfile

internal val IconsPage = CatalogPage(
    id = "icons",
    section = CatalogSection.Foundations,
    name = "Icons",
    description = "KotogramIcons: line icons drawn from lines, circles and arcs at Telegram for macOS's sizes and " +
        "weights, in the middle of Compose's 24-dp canvas. The chat list row's icons are Telegram for iOS's, at " +
        "their own sizes.",
    examples = listOf(
        CatalogExample("Common") { IconGrid(Common, 24.dp) },
        CatalogExample("Sign-in") { IconGrid(SignIn, 24.dp) },
        CatalogExample("Chat list") { IconGrid(ChatList, 24.dp) },
        CatalogExample("Chat") { IconGrid(Chat, 24.dp) },
        CatalogExample("Settings and profile") { IconGrid(SettingsAndProfile, 24.dp) },
        CatalogExample("At three times the size") { IconGrid(AllIcons, 72.dp) },
        CatalogExample("Chat list row") { IconGrid(ChatListRow, cellWidth = WideCellWidth) },
        CatalogExample("Avatars") { IconGrid(Avatars, cellWidth = WideCellWidth) },
        CatalogExample("Chat list row at three times the size") {
            IconGrid(ChatListRow, scale = 3f, cellWidth = WideCellWidth)
        },
    ),
)

@Composable
private fun IconGrid(
    icons: List<Pair<String, ImageVector>>,
    size: Dp = Dp.Unspecified,
    scale: Float = 1f,
    cellWidth: Dp = CellWidth,
) {
    val spacing = KotogramTheme.spacing
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        icons.forEach { (name, icon) ->
            IconSample(name, icon, if (size == Dp.Unspecified) icon.defaultWidth * scale else size, cellWidth)
        }
    }
}

@Composable
private fun IconSample(
    name: String,
    icon: ImageVector,
    size: Dp,
    cellWidth: Dp,
) {
    Column(
        modifier = Modifier.width(maxOf(size, cellWidth)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.extraSmall),
    ) {
        Icon(icon, contentDescription = name, modifier = Modifier.size(size))
        Text(name, style = KotogramTheme.typography.bodySmall, color = KotogramTheme.colors.onSurfaceVariant)
    }
}

private val CellWidth = 80.dp
private val WideCellWidth = 128.dp

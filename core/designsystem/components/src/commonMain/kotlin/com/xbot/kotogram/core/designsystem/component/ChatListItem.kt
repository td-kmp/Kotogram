package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import kotlin.math.roundToInt

enum class MessageStatus { Sending, Sent, Read, Failed }

enum class ChatListItemSeparator { Inset, FullWidth }

@Immutable
sealed interface TitleMark {
    data object Verified : TitleMark

    data object Premium : TitleMark

    @Immutable
    class Warning(
        val label: String,
    ) : TitleMark
}

@Stable
class ChatListItemScope internal constructor(
    internal val texts: ChatListItemTexts,
    internal val containerColor: Color,
) {
    internal val style: ChatListItemStyleScope get() = texts.style
}

@Composable
fun ChatListItem(
    avatar: @Composable ChatListItemScope.() -> Unit,
    title: @Composable ChatListItemScope.() -> Unit,
    text: @Composable ChatListItemScope.() -> Unit,
    modifier: Modifier = Modifier,
    date: @Composable (ChatListItemScope.() -> Unit)? = null,
    badges: @Composable (ChatListItemScope.() -> Unit)? = null,
    pinned: Boolean = false,
    separator: ChatListItemSeparator = ChatListItemSeparator.Inset,
    onClick: (() -> Unit)? = null,
    style: ChatListItemStyle = ChatListItemStyle,
    interactionSource: MutableInteractionSource? = null,
) {
    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::ChatListItemStyleScope, ChatListItemStyle.Adaptive then style)
    val texts = rememberChatListItemTexts(style)
    val container by animateColorAsState(
        targetValue = if (pinned) style.pinnedContainerColor else style.containerColor,
        animationSpec = tween(PIN_MILLIS, easing = EaseInOut),
    )
    val scope = remember(texts, container) { ChatListItemScope(texts, container) }
    val clickable = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = KotogramTheme.indication,
            onClick = onClick,
        )
    } else {
        Modifier
    }
    Layout(
        modifier = modifier
            .background(container)
            .then(clickable)
            .drawBehind {
                val inset = separator == ChatListItemSeparator.Inset
                val start = if (inset) (style.avatarStart + style.avatarSize + AvatarGap).toPx() else 0f
                val end = if (inset) style.separatorEnd.toPx() else 0f
                drawRect(style.separatorColor, Offset(start, size.height - 1f), Size(size.width - start - end, 1f))
            },
        content = {
            Box(Modifier.layoutId(Slot.Avatar).size(style.avatarSize), contentAlignment = Alignment.Center) {
                scope.avatar()
            }
            Box(Modifier.layoutId(Slot.Title)) { scope.title() }
            if (date != null) Box(Modifier.layoutId(Slot.Date)) { scope.date() }
            Box(Modifier.layoutId(Slot.Text)) { scope.text() }
            if (badges != null) Box(Modifier.layoutId(Slot.Badges)) { scope.badges() }
        },
        measurePolicy = remember(style) { ChatListItemMeasurePolicy(style) },
    )
}

private class ChatListItemMeasurePolicy(
    private val style: ChatListItemStyleScope,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        fun slot(slot: Slot) = measurables.firstOrNull { it.layoutId == slot }
        val width = constraints.maxWidth
        val contentLeft = (style.avatarStart + style.avatarSize + AvatarGap).roundToPx()
        val contentRight = width - style.contentEnd.roundToPx()
        val contentWidth = (contentRight - contentLeft).coerceAtLeast(0)
        val date = slot(Slot.Date)?.measure(Constraints())
        val badges = slot(Slot.Badges)?.measure(Constraints())
        val dateReserve = date?.let { it.width + style.titleDateGap.roundToPx() } ?: 0
        val title = slot(Slot.Title)!!.measure(Constraints(maxWidth = (contentWidth - dateReserve).coerceAtLeast(0)))
        val badgeReserve = badges?.let { it.width + BadgeTextGap.roundToPx() } ?: 0
        val text = slot(Slot.Text)!!.measure(Constraints(maxWidth = (contentWidth - badgeReserve).coerceAtLeast(0)))
        val avatar = slot(Slot.Avatar)!!.measure(Constraints())
        val titleBaseline = style.titleBaseline.roundToPx()
        val textBaseline = style.textBaseline.toPx()
        val height = (textBaseline + style.textLineHeight.toPx() + style.bottomPadding.toPx()).roundToInt()
        return layout(width, height) {
            avatar.place(style.avatarStart.roundToPx(), (height - avatar.height) / 2)
            title.place(contentLeft, titleBaseline - title.baseline())
            date?.place(width - style.dateEnd.roundToPx() - date.width, titleBaseline - date.baseline())
            text.place(contentLeft, textBaseline.roundToInt() - text.baseline())
            badges?.place(contentRight - badges.width, height - style.badgeBottom.roundToPx() - badges.height)
        }
    }

    private fun Placeable.baseline(): Int = get(FirstBaseline).takeIf { it != AlignmentLine.Unspecified } ?: 0
}

@Immutable
internal class ChatListItemTexts(
    val style: ChatListItemStyleScope,
    val title: TextStyle,
    val date: TextStyle,
    val author: TextStyle,
    val draft: TextStyle,
    val text: TextStyle,
    val badge: TextStyle,
    val smallLabel: TextStyle,
    val ellipsisWidth: Float,
    val textInk: Float,
    val textEllipsis: Float,
)

@Composable
private fun rememberChatListItemTexts(style: ChatListItemStyleScope): ChatListItemTexts {
    val measurer = rememberTextMeasurer()
    return remember(style) {
        val lines = TextStyle(
            lineHeight = style.textLineHeight,
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Top, LineHeightStyle.Trim.None),
        )
        ChatListItemTexts(
            style = style,
            title = style.titleTextStyle,
            date = style.dateTextStyle,
            author = style.authorTextStyle.merge(lines),
            draft = style.draftTextStyle.merge(lines),
            text = style.textTextStyle.merge(lines),
            badge = style.badgeTextStyle,
            smallLabel = style.smallLabelTextStyle,
            ellipsisWidth = measurer.measure("…", style.titleTextStyle).size.width.toFloat(),
            textInk = measurer.measure(" ", style.textTextStyle).size.height.toFloat(),
            textEllipsis = measurer.measure("…", style.textTextStyle).size.width.toFloat(),
        )
    }
}

private enum class Slot { Avatar, Title, Date, Text, Badges }

@Suppress("MagicNumber")
fun interface ChatListItemStyle : ComponentStyle<ChatListItemStyleScope> {
    companion object : ChatListItemStyle {
        override fun ChatListItemStyleScope.applyStyle() {}

        val Default = ChatListItemStyle {
            avatarSize(60.dp)
            avatarStart(16.dp)
            contentEnd(16.dp)
            dateEnd(16.667.dp)
            titleBaseline(23.333.sp)
            textBaseline(43.333.sp)
            textLineHeight(20.sp)
            bottomPadding(12.667.sp)
            titleDateGap(10.dp)
            emojiStatusSize(20.dp)
            verifiedSize(16.dp)
            mutedIconSize(24.dp)
            statusIconSize(24.dp)
            badgeGap(4.dp)
            badgeBottom(17.dp)
            thumbnailSize(18.dp)
            separatorEnd(16.dp)
            containerColor(theme.colors.surface)
            pinnedContainerColor(theme.colors.surfaceContainerHigh)
            separatorColor(theme.colors.outlineVariant)
            iconColor(theme.colors.outline)
            checkColor(theme.colors.primary)
            pendingColor(theme.colors.onSurfaceVariant)
            failedColor(theme.colors.error)
            failedContentColor(theme.colors.onError)
            verifiedColor(theme.colors.primary)
            verifiedContentColor(theme.colors.onPrimary)
            badgeColor(theme.colors.primary)
            badgeContentColor(theme.colors.onPrimary)
            mutedBadgeColor(theme.colors.outline)
            mutedBadgeContentColor(theme.colors.surface)
            reactionBadgeColor(theme.colors.error)
            onlineColor(theme.colors.tertiary)
            storyUnseenColors(theme.colors.tertiary to theme.colors.primary)
            storyCloseFriendsColors(
                lerp(theme.colors.tertiary, Color.White, LIGHTER) to lerp(theme.colors.tertiary, Color.Black, DARKER),
            )
            storyLiveColors(theme.colors.error to theme.colors.error)
            storySeenColor(theme.colors.outline.copy(alpha = SEEN_ALPHA).compositeOver(theme.colors.surface))
            voiceChatBarColor(theme.colors.onTertiary)
            autoDeleteContentColor(Color.White)
            subscriptionColor(Color(0xFFFFB10D))
            liveBadgeColor(theme.colors.error)
            liveBadgeContentColor(theme.colors.onError)
            secretColor(theme.colors.tertiary)
            scamColor(theme.colors.error)
            premiumColor(theme.colors.primary)
            labelColor(theme.colors.onSurface.copy(alpha = LABEL_ALPHA))
            tagColors(
                listOf(
                    Color(0xFFCC5049),
                    Color(0xFFD67722),
                    Color(0xFF955CDB),
                    Color(0xFF40A920),
                    Color(0xFF309EBA),
                    Color(0xFF368AD1),
                    Color(0xFFC7508B),
                ),
            )
            tagOverflowColor(theme.colors.onSurfaceVariant)
            generalTopicColor(theme.colors.outline)
            openAppColor(theme.colors.primary)
            openAppContentColor(theme.colors.onPrimary)
            titleTextStyle(theme.typography.titleMedium.merge(color = theme.colors.onSurface))
            textTextStyle(theme.typography.bodyMedium.merge(color = theme.colors.onSurfaceVariant))
            authorTextStyle(theme.typography.bodyMedium.merge(color = theme.colors.onSurface))
            draftTextStyle(theme.typography.bodyMedium.merge(color = theme.colors.error))
            dateTextStyle(theme.typography.labelMedium.merge(color = theme.colors.onSurfaceVariant))
            badgeTextStyle(theme.typography.labelSmallEmphasized.merge(fontFeatureSettings = "tnum"))
            smallLabelTextStyle(theme.typography.labelSmallEmphasized)
            openAppTextStyle(theme.typography.titleSmall)
            topicHighlightColor(lerp(theme.colors.surfaceContainerHigh, theme.colors.outline, HIGHLIGHT_MIX))
            topicArrowColor(theme.colors.outline.copy(alpha = TOPIC_ARROW_ALPHA))
        }

        val Adaptive = Default then ChatListItemStyle {
            laptop {
                avatarSize(50.dp)
                avatarStart(10.dp)
                contentEnd(9.dp)
                dateEnd(9.dp)
                titleBaseline(21.333.sp)
                textBaseline(40.sp)
                textLineHeight(16.sp)
                bottomPadding(14.sp)
                titleDateGap(9.dp)
                emojiStatusSize(16.dp)
                verifiedSize(14.dp)
                mutedIconSize(20.dp)
                statusIconSize(20.dp)
                badgeGap(9.dp)
                badgeBottom(10.dp)
                thumbnailSize(16.dp)
                separatorEnd(0.dp)
            }
        }
    }
}

infix fun ChatListItemStyle.then(other: ChatListItemStyle): ChatListItemStyle = when {
    other === ChatListItemStyle -> this

    this === ChatListItemStyle -> other

    else -> ChatListItemStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
class ChatListItemStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<ChatListItemStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var avatarSize: Dp = Dp.Unspecified
        private set

    var avatarStart: Dp = Dp.Unspecified
        private set

    var contentEnd: Dp = Dp.Unspecified
        private set

    var dateEnd: Dp = Dp.Unspecified
        private set

    var titleBaseline: TextUnit = TextUnit.Unspecified
        private set

    var textBaseline: TextUnit = TextUnit.Unspecified
        private set

    var textLineHeight: TextUnit = TextUnit.Unspecified
        private set

    var bottomPadding: TextUnit = TextUnit.Unspecified
        private set

    var titleDateGap: Dp = Dp.Unspecified
        private set

    var emojiStatusSize: Dp = Dp.Unspecified
        private set

    var verifiedSize: Dp = Dp.Unspecified
        private set

    var mutedIconSize: Dp = Dp.Unspecified
        private set

    var statusIconSize: Dp = Dp.Unspecified
        private set

    var badgeGap: Dp = Dp.Unspecified
        private set

    var badgeBottom: Dp = Dp.Unspecified
        private set

    var thumbnailSize: Dp = Dp.Unspecified
        private set

    var separatorEnd: Dp = Dp.Unspecified
        private set

    var containerColor: Color = Color.Unspecified
        private set

    var pinnedContainerColor: Color = Color.Unspecified
        private set

    var separatorColor: Color = Color.Unspecified
        private set

    var iconColor: Color = Color.Unspecified
        private set

    var checkColor: Color = Color.Unspecified
        private set

    var pendingColor: Color = Color.Unspecified
        private set

    var failedColor: Color = Color.Unspecified
        private set

    var failedContentColor: Color = Color.Unspecified
        private set

    var verifiedColor: Color = Color.Unspecified
        private set

    var verifiedContentColor: Color = Color.Unspecified
        private set

    var badgeColor: Color = Color.Unspecified
        private set

    var badgeContentColor: Color = Color.Unspecified
        private set

    var mutedBadgeColor: Color = Color.Unspecified
        private set

    var mutedBadgeContentColor: Color = Color.Unspecified
        private set

    var reactionBadgeColor: Color = Color.Unspecified
        private set

    var onlineColor: Color = Color.Unspecified
        private set

    var storyUnseenColors: Pair<Color, Color> = Color.Unspecified to Color.Unspecified
        private set

    var storyCloseFriendsColors: Pair<Color, Color> = Color.Unspecified to Color.Unspecified
        private set

    var storyLiveColors: Pair<Color, Color> = Color.Unspecified to Color.Unspecified
        private set

    var storySeenColor: Color = Color.Unspecified
        private set

    var voiceChatBarColor: Color = Color.Unspecified
        private set

    var autoDeleteContentColor: Color = Color.Unspecified
        private set

    var subscriptionColor: Color = Color.Unspecified
        private set

    var liveBadgeColor: Color = Color.Unspecified
        private set

    var liveBadgeContentColor: Color = Color.Unspecified
        private set

    var secretColor: Color = Color.Unspecified
        private set

    var scamColor: Color = Color.Unspecified
        private set

    var premiumColor: Color = Color.Unspecified
        private set

    var labelColor: Color = Color.Unspecified
        private set

    var tagColors: List<Color> = emptyList()
        private set

    var tagOverflowColor: Color = Color.Unspecified
        private set

    var generalTopicColor: Color = Color.Unspecified
        private set

    var openAppColor: Color = Color.Unspecified
        private set

    var openAppContentColor: Color = Color.Unspecified
        private set

    var titleTextStyle: TextStyle = TextStyle.Default
        private set

    var textTextStyle: TextStyle = TextStyle.Default
        private set

    var authorTextStyle: TextStyle = TextStyle.Default
        private set

    var draftTextStyle: TextStyle = TextStyle.Default
        private set

    var dateTextStyle: TextStyle = TextStyle.Default
        private set

    var badgeTextStyle: TextStyle = TextStyle.Default
        private set

    var smallLabelTextStyle: TextStyle = TextStyle.Default
        private set

    var openAppTextStyle: TextStyle = TextStyle.Default
        private set

    var topicHighlightColor: Color = Color.Unspecified
        private set

    var topicArrowColor: Color = Color.Unspecified
        private set

    fun avatarSize(value: Dp) {
        avatarSize = value
    }

    fun avatarStart(value: Dp) {
        avatarStart = value
    }

    fun contentEnd(value: Dp) {
        contentEnd = value
    }

    fun dateEnd(value: Dp) {
        dateEnd = value
    }

    fun titleBaseline(value: TextUnit) {
        titleBaseline = value
    }

    fun textBaseline(value: TextUnit) {
        textBaseline = value
    }

    fun textLineHeight(value: TextUnit) {
        textLineHeight = value
    }

    fun bottomPadding(value: TextUnit) {
        bottomPadding = value
    }

    fun titleDateGap(value: Dp) {
        titleDateGap = value
    }

    fun emojiStatusSize(value: Dp) {
        emojiStatusSize = value
    }

    fun verifiedSize(value: Dp) {
        verifiedSize = value
    }

    fun mutedIconSize(value: Dp) {
        mutedIconSize = value
    }

    fun statusIconSize(value: Dp) {
        statusIconSize = value
    }

    fun badgeGap(value: Dp) {
        badgeGap = value
    }

    fun badgeBottom(value: Dp) {
        badgeBottom = value
    }

    fun thumbnailSize(value: Dp) {
        thumbnailSize = value
    }

    fun separatorEnd(value: Dp) {
        separatorEnd = value
    }

    fun containerColor(value: Color) {
        containerColor = value
    }

    fun pinnedContainerColor(value: Color) {
        pinnedContainerColor = value
    }

    fun separatorColor(value: Color) {
        separatorColor = value
    }

    fun iconColor(value: Color) {
        iconColor = value
    }

    fun checkColor(value: Color) {
        checkColor = value
    }

    fun pendingColor(value: Color) {
        pendingColor = value
    }

    fun failedColor(value: Color) {
        failedColor = value
    }

    fun failedContentColor(value: Color) {
        failedContentColor = value
    }

    fun verifiedColor(value: Color) {
        verifiedColor = value
    }

    fun verifiedContentColor(value: Color) {
        verifiedContentColor = value
    }

    fun badgeColor(value: Color) {
        badgeColor = value
    }

    fun badgeContentColor(value: Color) {
        badgeContentColor = value
    }

    fun mutedBadgeColor(value: Color) {
        mutedBadgeColor = value
    }

    fun mutedBadgeContentColor(value: Color) {
        mutedBadgeContentColor = value
    }

    fun reactionBadgeColor(value: Color) {
        reactionBadgeColor = value
    }

    fun onlineColor(value: Color) {
        onlineColor = value
    }

    fun storyUnseenColors(value: Pair<Color, Color>) {
        storyUnseenColors = value
    }

    fun storyCloseFriendsColors(value: Pair<Color, Color>) {
        storyCloseFriendsColors = value
    }

    fun storyLiveColors(value: Pair<Color, Color>) {
        storyLiveColors = value
    }

    fun storySeenColor(value: Color) {
        storySeenColor = value
    }

    fun voiceChatBarColor(value: Color) {
        voiceChatBarColor = value
    }

    fun autoDeleteContentColor(value: Color) {
        autoDeleteContentColor = value
    }

    fun subscriptionColor(value: Color) {
        subscriptionColor = value
    }

    fun liveBadgeColor(value: Color) {
        liveBadgeColor = value
    }

    fun liveBadgeContentColor(value: Color) {
        liveBadgeContentColor = value
    }

    fun secretColor(value: Color) {
        secretColor = value
    }

    fun scamColor(value: Color) {
        scamColor = value
    }

    fun premiumColor(value: Color) {
        premiumColor = value
    }

    fun labelColor(value: Color) {
        labelColor = value
    }

    fun tagColors(value: List<Color>) {
        tagColors = value
    }

    fun tagOverflowColor(value: Color) {
        tagOverflowColor = value
    }

    fun generalTopicColor(value: Color) {
        generalTopicColor = value
    }

    fun openAppColor(value: Color) {
        openAppColor = value
    }

    fun openAppContentColor(value: Color) {
        openAppContentColor = value
    }

    fun titleTextStyle(value: TextStyle) {
        titleTextStyle = value
    }

    fun textTextStyle(value: TextStyle) {
        textTextStyle = value
    }

    fun authorTextStyle(value: TextStyle) {
        authorTextStyle = value
    }

    fun draftTextStyle(value: TextStyle) {
        draftTextStyle = value
    }

    fun dateTextStyle(value: TextStyle) {
        dateTextStyle = value
    }

    fun badgeTextStyle(value: TextStyle) {
        badgeTextStyle = value
    }

    fun smallLabelTextStyle(value: TextStyle) {
        smallLabelTextStyle = value
    }

    fun openAppTextStyle(value: TextStyle) {
        openAppTextStyle = value
    }

    fun topicHighlightColor(value: Color) {
        topicHighlightColor = value
    }

    fun topicArrowColor(value: Color) {
        topicArrowColor = value
    }
}

private val AvatarGap = 10.dp
private val BadgeTextGap = 5.dp
private const val LIGHTER = 0.2f
private const val PIN_MILLIS = 300
private val EaseInOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
private const val DARKER = 0.15f
private const val SEEN_ALPHA = 0.4f
private const val LABEL_ALPHA = 0.4f
private const val HIGHLIGHT_MIX = 0.1f
private const val TOPIC_ARROW_ALPHA = 0.56f

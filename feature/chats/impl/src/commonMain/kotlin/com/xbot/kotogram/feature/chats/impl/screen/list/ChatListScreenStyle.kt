package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver

@Suppress("MagicNumber")
internal fun interface ChatListScreenStyle : ComponentStyle<ChatListScreenStyleScope> {
    companion object : ChatListScreenStyle {
        override fun ChatListScreenStyleScope.applyStyle() {}

        val Default = ChatListScreenStyle {
            rowHeight(Dp.Unspecified)
            titleLineSpacing(0.12f)
            previewLineSpacing(0.2f)
            avatarSize(60.dp)
            avatarStart(16.dp)
            contentStart(86.dp)
            contentEnd(16.dp)
            titleTop(8.dp)
            dateTop(10.dp)
            authorGap(0.dp)
            textGap(0.dp)
            authorToTextGap(1.dp)
            titleDateGap(10.dp)
            titleIconGap(3.dp)
            emojiStatusSize(20.dp)
            mutedIconSize(14.dp)
            mutedIconGap(3.dp)
            statusIconSize(24.dp)
            statusGap(0.dp)
            badgeHeight(20.dp)
            badgePadding(5.dp)
            badgeGap(6.dp)
            badgeBottom(17.dp)
            badgeTextReserve(5.dp)
            pinnedIconSize(16.dp)
            thumbnailSize(18.dp)
            thumbnailSpacing(2.dp)
            thumbnailTrailing(5.dp)
            thumbnailCorner(3.dp)
            separatorStart(86.dp)
            separatorEnd(16.dp)
            tabsPadding(PaddingValues(horizontal = 16.dp, vertical = 8.dp))
            tabsInnerPadding(4.dp)
            tabHeight(36.dp)
            tabContentPadding(16.dp)
            tabBadgeHeight(18.dp)
            tabBadgeGap(6.dp)
            badgeColor(theme.colors.primary)
            mutedBadgeColor(theme.colors.outline)
            badgeContentColor(Color.White)
            iconColor(theme.colors.onSurfaceVariant)
            checkColor(theme.colors.primary)
            separatorColor(theme.colors.outlineVariant)
            tabsColor(theme.colors.surfaceContainerHigh)
            selectedTabColor(theme.colors.surfaceContainerHighest)
            titleTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.colors.onSurface,
                ),
            )
            previewTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 15.sp,
                    color = theme.colors.onSurfaceVariant,
                ),
            )
            authorTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 15.sp,
                    color = theme.colors.onSurface,
                ),
            )
            draftTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 15.sp,
                    color = theme.colors.error,
                ),
            )
            dateTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 14.sp,
                    color = theme.colors.onSurfaceVariant,
                ),
            )
            badgeTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum",
                    color = Color.White,
                ),
            )
            tabTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 17.sp,
                    color = theme.colors.onSurface,
                ),
            )
            tabBadgeTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 14.sp,
                    color = Color.White,
                ),
            )
        }

        val Adaptive = Default then ChatListScreenStyle {
            laptop {
                rowHeight(70.dp)
                titleLineSpacing(0f)
                previewLineSpacing(0f)
                avatarSize(50.dp)
                avatarStart(10.dp)
                contentStart(70.dp)
                contentEnd(9.dp)
                titleTop(8.dp)
                dateTop(9.dp)
                authorGap(3.dp)
                textGap(2.dp)
                authorToTextGap(1.dp)
                titleDateGap(9.dp)
                titleIconGap(4.dp)
                emojiStatusSize(16.dp)
                mutedIconSize(12.dp)
                mutedIconGap(4.dp)
                statusIconSize(16.dp)
                statusGap(2.dp)
                badgeHeight(20.dp)
                badgePadding(4.dp)
                badgeGap(9.dp)
                badgeBottom(10.dp)
                badgeTextReserve(5.dp)
                pinnedIconSize(20.dp)
                thumbnailSize(16.dp)
                thumbnailSpacing(2.dp)
                thumbnailTrailing(4.dp)
                separatorStart(70.dp)
                separatorEnd(0.dp)
                titleTextStyle(
                    theme.typography.bodyLarge.merge(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.colors.onSurface,
                    ),
                )
                previewTextStyle(
                    theme.typography.bodyLarge.merge(
                        fontSize = 13.sp,
                        color = theme.colors.onSurfaceVariant,
                    ),
                )
                authorTextStyle(
                    theme.typography.bodyLarge.merge(
                        fontSize = 13.sp,
                        color = theme.colors.onSurface,
                    ),
                )
                draftTextStyle(
                    theme.typography.bodyLarge.merge(
                        fontSize = 13.sp,
                        color = theme.colors.error,
                    ),
                )
                dateTextStyle(
                    theme.typography.bodyLarge.merge(
                        fontSize = 12.sp,
                        color = theme.colors.onSurfaceVariant,
                    ),
                )
                badgeTextStyle(
                    theme.typography.bodyLarge.merge(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFeatureSettings = "tnum",
                        color = Color.White,
                    ),
                )
            }
        }
    }
}

internal infix fun ChatListScreenStyle.then(other: ChatListScreenStyle): ChatListScreenStyle = when {
    other === ChatListScreenStyle -> this

    this === ChatListScreenStyle -> other

    else -> ChatListScreenStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
internal class ChatListScreenStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<ChatListScreenStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var rowHeight: Dp = Dp.Unspecified
        private set

    var titleLineSpacing: Float = 0f
        private set

    var previewLineSpacing: Float = 0f
        private set

    var avatarSize: Dp = Dp.Unspecified
        private set

    var avatarStart: Dp = Dp.Unspecified
        private set

    var contentStart: Dp = Dp.Unspecified
        private set

    var contentEnd: Dp = Dp.Unspecified
        private set

    var titleTop: Dp = Dp.Unspecified
        private set

    var dateTop: Dp = Dp.Unspecified
        private set

    var authorGap: Dp = Dp.Unspecified
        private set

    var textGap: Dp = Dp.Unspecified
        private set

    var authorToTextGap: Dp = Dp.Unspecified
        private set

    var titleDateGap: Dp = Dp.Unspecified
        private set

    var titleIconGap: Dp = Dp.Unspecified
        private set

    var emojiStatusSize: Dp = Dp.Unspecified
        private set

    var mutedIconSize: Dp = Dp.Unspecified
        private set

    var mutedIconGap: Dp = Dp.Unspecified
        private set

    var statusIconSize: Dp = Dp.Unspecified
        private set

    var statusGap: Dp = Dp.Unspecified
        private set

    var badgeHeight: Dp = Dp.Unspecified
        private set

    var badgePadding: Dp = Dp.Unspecified
        private set

    var badgeGap: Dp = Dp.Unspecified
        private set

    var badgeBottom: Dp = Dp.Unspecified
        private set

    var badgeTextReserve: Dp = Dp.Unspecified
        private set

    var pinnedIconSize: Dp = Dp.Unspecified
        private set

    var thumbnailSize: Dp = Dp.Unspecified
        private set

    var thumbnailSpacing: Dp = Dp.Unspecified
        private set

    var thumbnailTrailing: Dp = Dp.Unspecified
        private set

    var thumbnailCorner: Dp = Dp.Unspecified
        private set

    var separatorStart: Dp = Dp.Unspecified
        private set

    var separatorEnd: Dp = Dp.Unspecified
        private set

    var tabsPadding: PaddingValues = PaddingValues()
        private set

    var tabsInnerPadding: Dp = Dp.Unspecified
        private set

    var tabHeight: Dp = Dp.Unspecified
        private set

    var tabContentPadding: Dp = Dp.Unspecified
        private set

    var tabBadgeHeight: Dp = Dp.Unspecified
        private set

    var tabBadgeGap: Dp = Dp.Unspecified
        private set

    var badgeColor: Color = Color.Unspecified
        private set

    var mutedBadgeColor: Color = Color.Unspecified
        private set

    var badgeContentColor: Color = Color.Unspecified
        private set

    var iconColor: Color = Color.Unspecified
        private set

    var checkColor: Color = Color.Unspecified
        private set

    var separatorColor: Color = Color.Unspecified
        private set

    var tabsColor: Color = Color.Unspecified
        private set

    var selectedTabColor: Color = Color.Unspecified
        private set

    var titleTextStyle: TextStyle = TextStyle.Default
        private set

    var previewTextStyle: TextStyle = TextStyle.Default
        private set

    var authorTextStyle: TextStyle = TextStyle.Default
        private set

    var draftTextStyle: TextStyle = TextStyle.Default
        private set

    var dateTextStyle: TextStyle = TextStyle.Default
        private set

    var badgeTextStyle: TextStyle = TextStyle.Default
        private set

    var tabTextStyle: TextStyle = TextStyle.Default
        private set

    var tabBadgeTextStyle: TextStyle = TextStyle.Default
        private set

    fun rowHeight(space: Dp) {
        rowHeight = space
    }

    fun titleLineSpacing(factor: Float) {
        titleLineSpacing = factor
    }

    fun previewLineSpacing(factor: Float) {
        previewLineSpacing = factor
    }

    fun avatarSize(space: Dp) {
        avatarSize = space
    }

    fun avatarStart(space: Dp) {
        avatarStart = space
    }

    fun contentStart(space: Dp) {
        contentStart = space
    }

    fun contentEnd(space: Dp) {
        contentEnd = space
    }

    fun titleTop(space: Dp) {
        titleTop = space
    }

    fun dateTop(space: Dp) {
        dateTop = space
    }

    fun authorGap(space: Dp) {
        authorGap = space
    }

    fun textGap(space: Dp) {
        textGap = space
    }

    fun authorToTextGap(space: Dp) {
        authorToTextGap = space
    }

    fun titleDateGap(space: Dp) {
        titleDateGap = space
    }

    fun titleIconGap(space: Dp) {
        titleIconGap = space
    }

    fun emojiStatusSize(space: Dp) {
        emojiStatusSize = space
    }

    fun mutedIconSize(space: Dp) {
        mutedIconSize = space
    }

    fun mutedIconGap(gap: Dp) {
        mutedIconGap = gap
    }

    fun statusIconSize(space: Dp) {
        statusIconSize = space
    }

    fun statusGap(space: Dp) {
        statusGap = space
    }

    fun badgeHeight(space: Dp) {
        badgeHeight = space
    }

    fun badgePadding(space: Dp) {
        badgePadding = space
    }

    fun badgeGap(space: Dp) {
        badgeGap = space
    }

    fun badgeBottom(space: Dp) {
        badgeBottom = space
    }

    fun badgeTextReserve(space: Dp) {
        badgeTextReserve = space
    }

    fun pinnedIconSize(space: Dp) {
        pinnedIconSize = space
    }

    fun thumbnailSize(space: Dp) {
        thumbnailSize = space
    }

    fun thumbnailSpacing(space: Dp) {
        thumbnailSpacing = space
    }

    fun thumbnailTrailing(space: Dp) {
        thumbnailTrailing = space
    }

    fun thumbnailCorner(space: Dp) {
        thumbnailCorner = space
    }

    fun separatorStart(space: Dp) {
        separatorStart = space
    }

    fun separatorEnd(space: Dp) {
        separatorEnd = space
    }

    fun tabsPadding(padding: PaddingValues) {
        tabsPadding = padding
    }

    fun tabsInnerPadding(space: Dp) {
        tabsInnerPadding = space
    }

    fun tabHeight(space: Dp) {
        tabHeight = space
    }

    fun tabContentPadding(space: Dp) {
        tabContentPadding = space
    }

    fun tabBadgeHeight(space: Dp) {
        tabBadgeHeight = space
    }

    fun tabBadgeGap(space: Dp) {
        tabBadgeGap = space
    }

    fun badgeColor(color: Color) {
        badgeColor = color
    }

    fun mutedBadgeColor(color: Color) {
        mutedBadgeColor = color
    }

    fun badgeContentColor(color: Color) {
        badgeContentColor = color
    }

    fun iconColor(color: Color) {
        iconColor = color
    }

    fun checkColor(color: Color) {
        checkColor = color
    }

    fun separatorColor(color: Color) {
        separatorColor = color
    }

    fun tabsColor(color: Color) {
        tabsColor = color
    }

    fun selectedTabColor(color: Color) {
        selectedTabColor = color
    }

    fun titleTextStyle(style: TextStyle) {
        titleTextStyle = style
    }

    fun previewTextStyle(style: TextStyle) {
        previewTextStyle = style
    }

    fun authorTextStyle(style: TextStyle) {
        authorTextStyle = style
    }

    fun draftTextStyle(style: TextStyle) {
        draftTextStyle = style
    }

    fun dateTextStyle(style: TextStyle) {
        dateTextStyle = style
    }

    fun badgeTextStyle(style: TextStyle) {
        badgeTextStyle = style
    }

    fun tabTextStyle(style: TextStyle) {
        tabTextStyle = style
    }

    fun tabBadgeTextStyle(style: TextStyle) {
        tabBadgeTextStyle = style
    }
}

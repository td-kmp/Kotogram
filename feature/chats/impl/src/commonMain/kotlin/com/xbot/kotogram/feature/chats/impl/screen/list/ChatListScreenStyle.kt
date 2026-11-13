package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
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
            previewEmojiSize(18.dp)
            tabEmojiSize(20.dp)
            tabBadgePadding(5.dp)
            tabsColor(theme.colors.surfaceContainerHigh)
            selectedTabColor(theme.colors.surfaceContainerHighest)
            tabBadgeColor(theme.colors.primary)
            tabMutedBadgeColor(theme.colors.outline)
            tabTextStyle(theme.typography.bodyLarge.merge(color = theme.colors.onSurface))
            tabBadgeTextStyle(theme.typography.labelMedium.merge(color = Color.White))
            connectionIndicatorSize(20.dp)
            connectionIndicatorGap(4.dp)
            connectionIndicatorColor(
                if (theme.colors.surface.luminance() < 0.5f) theme.colors.onSurface else theme.colors.onSurfaceVariant,
            )
        }

        val Adaptive = Default then ChatListScreenStyle {
            laptop {
                previewEmojiSize(16.dp)
                tabEmojiSize(16.dp)
                tabBadgePadding(4.dp)
                connectionIndicatorSize(16.dp)
                connectionIndicatorGap(6.dp)
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

    var previewEmojiSize: Dp = Dp.Unspecified
        private set

    var tabEmojiSize: Dp = Dp.Unspecified
        private set

    var tabBadgePadding: Dp = Dp.Unspecified
        private set

    var tabsColor: Color = Color.Unspecified
        private set

    var selectedTabColor: Color = Color.Unspecified
        private set

    var tabBadgeColor: Color = Color.Unspecified
        private set

    var tabMutedBadgeColor: Color = Color.Unspecified
        private set

    var tabTextStyle: TextStyle = TextStyle.Default
        private set

    var tabBadgeTextStyle: TextStyle = TextStyle.Default
        private set

    var connectionIndicatorSize: Dp = Dp.Unspecified
        private set

    var connectionIndicatorGap: Dp = Dp.Unspecified
        private set

    var connectionIndicatorColor: Color = Color.Unspecified
        private set

    fun previewEmojiSize(size: Dp) {
        previewEmojiSize = size
    }

    fun tabEmojiSize(size: Dp) {
        tabEmojiSize = size
    }

    fun tabBadgePadding(space: Dp) {
        tabBadgePadding = space
    }

    fun tabsColor(color: Color) {
        tabsColor = color
    }

    fun selectedTabColor(color: Color) {
        selectedTabColor = color
    }

    fun tabBadgeColor(color: Color) {
        tabBadgeColor = color
    }

    fun tabMutedBadgeColor(color: Color) {
        tabMutedBadgeColor = color
    }

    fun tabTextStyle(style: TextStyle) {
        tabTextStyle = style
    }

    fun tabBadgeTextStyle(style: TextStyle) {
        tabBadgeTextStyle = style
    }

    fun connectionIndicatorSize(size: Dp) {
        connectionIndicatorSize = size
    }

    fun connectionIndicatorGap(gap: Dp) {
        connectionIndicatorGap = gap
    }

    fun connectionIndicatorColor(color: Color) {
        connectionIndicatorColor = color
    }
}

package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.PreviewKotogram
import com.xbot.kotogram.core.designsystem.PreviewKotogramLaptop
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.designsystem.theme.currentPlatform
import androidx.compose.material3.Icon as MaterialIcon

enum class ListItemPosition { Single, First, Middle, Last }

@Composable
fun ListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    position: ListItemPosition? = null,
    style: ListItemStyle = ListItemStyle,
    interactionSource: MutableInteractionSource? = null,
) {
    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::ListItemStyleScope, ListItemStyle.Adaptive then style)
    val hasDivider = position == ListItemPosition.First || position == ListItemPosition.Middle
    val twoLine = supportingContent != null || overlineContent != null
    val padding = if (twoLine) style.twoLineContentPadding else style.contentPadding
    // Hack: the padding may sit lower than it sits high, as UIKit's two-line cell; what flanks the text stays centred in the row.
    val centring = (padding.calculateBottomPadding() - padding.calculateTopPadding()) / 2
    var rowStart by remember { mutableFloatStateOf(0f) }
    var textStart by remember { mutableFloatStateOf(0f) }
    context(style) {
        Row(
            modifier = modifier
                .clip(style.groupShape.at(position))
                .background(if (enabled) style.containerColor else style.disabledContainerColor)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = KotogramTheme.indication,
                            enabled = enabled,
                            onClick = onClick,
                        )
                    } else {
                        Modifier
                    },
                )
                .onPlaced { rowStart = it.positionInRoot().x }
                .drawBehind {
                    if (hasDivider) {
                        val y = size.height - style.dividerThickness.toPx() / 2
                        drawLine(
                            color = style.dividerColor,
                            start = Offset(textStart - rowStart, y),
                            end = Offset(size.width, y),
                            strokeWidth = style.dividerThickness.toPx(),
                        )
                    }
                }
                .heightIn(min = if (twoLine) style.twoLineMinHeight else style.minHeight)
                .padding(padding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingContent != null) {
                Box(
                    Modifier.offset(y = centring).padding(end = style.leadingGap),
                    contentAlignment = Alignment.Center,
                ) {
                    ProvideContent(style.leadingColor.of(enabled), style.headlineTextStyle, leadingContent)
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .onPlaced { textStart = it.positionInRoot().x },
                verticalArrangement = Arrangement.spacedBy(style.lineGap),
            ) {
                if (overlineContent != null) {
                    ProvideContent(style.overlineColor.of(enabled), style.overlineTextStyle, overlineContent)
                }
                ProvideContent(style.headlineColor.of(enabled), style.headlineTextStyle, headlineContent)
                if (supportingContent != null) {
                    ProvideContent(style.supportingColor.of(enabled), style.supportingTextStyle, supportingContent)
                }
            }
            if (trailingContent != null) {
                Box(
                    Modifier.offset(y = centring).padding(start = style.trailingGap),
                    contentAlignment = Alignment.Center,
                ) {
                    ProvideContent(style.trailingColor.of(enabled), style.headlineTextStyle, trailingContent)
                }
            }
        }
    }
}

private fun CornerBasedShape.at(position: ListItemPosition?): Shape = when (position) {
    null, ListItemPosition.Middle -> RectangleShape
    ListItemPosition.Single -> this
    ListItemPosition.First -> copy(bottomStart = ZeroCornerSize, bottomEnd = ZeroCornerSize)
    ListItemPosition.Last -> copy(topStart = ZeroCornerSize, topEnd = ZeroCornerSize)
}

context(style: ListItemStyleScope)
private fun Color.of(enabled: Boolean): Color = if (enabled) this else style.disabledContentColor

@Composable
private fun ProvideContent(
    color: Color,
    style: TextStyle,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalContentColor provides color,
        LocalTextStyle provides LocalTextStyle.current.merge(style),
        content = content,
    )
}

@PreviewKotogram
@Composable
private fun ListItemPreview() {
    Column(Modifier.padding(KotogramTheme.spacing.large)) {
        ListItem(
            headlineContent = { Text("Notifications") },
            leadingContent = { MaterialIcon(KotogramIcons.Bell, contentDescription = null) },
            trailingContent = { MaterialIcon(KotogramIcons.ChevronRight, contentDescription = null) },
            onClick = {},
            position = ListItemPosition.First,
        )
        ListItem(
            headlineContent = { Text("Language") },
            supportingContent = { Text("English") },
            leadingContent = { MaterialIcon(KotogramIcons.Globe, contentDescription = null) },
            onClick = {},
            position = ListItemPosition.Middle,
        )
        ListItem(
            headlineContent = { Text("Sync contacts") },
            trailingContent = { Switch(checked = true, onCheckedChange = null) },
            position = ListItemPosition.Last,
        )
    }
}

@PreviewKotogramLaptop
@Composable
private fun ListItemLaptopPreview() = ListItemPreview()

@Suppress("MagicNumber")
fun interface ListItemStyle : ComponentStyle<ListItemStyleScope> {
    companion object : ListItemStyle {
        override fun ListItemStyleScope.applyStyle() {}

        val Default = ListItemStyle {
            val apple = currentPlatform.isApple
            minHeight(52.dp)
            contentPadding(PaddingValues(horizontal = 20.dp, vertical = 9.dp))
            twoLineMinHeight(62.dp)
            twoLineContentPadding(PaddingValues(start = 20.dp, top = 9.dp, end = 20.dp, bottom = 11.67.dp))
            lineGap(3.dp)
            groupShape(RoundedCornerShape(26.dp))
            leadingGap(12.dp)
            trailingGap(10.dp)
            dividerThickness(1.dp)
            dividerColor(theme.colors.outlineVariant)
            containerColor(theme.colors.surface)
            disabledContainerColor(theme.colors.surface)
            headlineColor(theme.colors.onSurface)
            overlineColor(theme.colors.onSurfaceVariant)
            supportingColor(theme.colors.onSurfaceVariant)
            leadingColor(theme.colors.onSurfaceVariant)
            trailingColor(theme.colors.onSurfaceVariant)
            disabledContentColor(theme.colors.onSurface.copy(alpha = 0.38f))
            overlineTextStyle(theme.typography.labelSmall)
            headlineTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 17.sp,
                    letterSpacing = if (apple) (-0.43).sp else TextUnit.Unspecified,
                ),
            )
            supportingTextStyle(
                theme.typography.bodyMedium.merge(
                    fontSize = 15.sp,
                    letterSpacing = if (apple) (-0.23).sp else TextUnit.Unspecified,
                ),
            )
        }

        val Adaptive = Default then ListItemStyle {
            laptop {
                minHeight(44.dp)
                contentPadding(PaddingValues(horizontal = 14.dp, vertical = 12.dp))
                twoLineMinHeight(44.dp)
                twoLineContentPadding(PaddingValues(horizontal = 14.dp, vertical = 12.dp))
                lineGap(2.dp)
                groupShape(theme.shapes.medium)
                headlineTextStyle(theme.typography.bodyLarge)
                supportingTextStyle(theme.typography.bodyMedium)
            }
        }
    }
}

infix fun ListItemStyle.then(other: ListItemStyle): ListItemStyle = when {
    other === ListItemStyle -> this

    this === ListItemStyle -> other

    else -> ListItemStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
class ListItemStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<ListItemStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var minHeight: Dp = Dp.Unspecified
        private set

    var contentPadding: PaddingValues = PaddingValues()
        private set

    var twoLineMinHeight: Dp = Dp.Unspecified
        private set

    var twoLineContentPadding: PaddingValues = PaddingValues()
        private set

    var leadingGap: Dp = Dp.Unspecified
        private set

    var trailingGap: Dp = Dp.Unspecified
        private set

    var lineGap: Dp = Dp.Unspecified
        private set

    var groupShape: CornerBasedShape = RoundedCornerShape(0.dp)
        private set

    var dividerThickness: Dp = Dp.Unspecified
        private set

    var dividerColor: Color = Color.Unspecified
        private set

    var containerColor: Color = Color.Unspecified
        private set

    var disabledContainerColor: Color = Color.Unspecified
        private set

    var headlineColor: Color = Color.Unspecified
        private set

    var overlineColor: Color = Color.Unspecified
        private set

    var supportingColor: Color = Color.Unspecified
        private set

    var leadingColor: Color = Color.Unspecified
        private set

    var trailingColor: Color = Color.Unspecified
        private set

    var disabledContentColor: Color = Color.Unspecified
        private set

    var headlineTextStyle: TextStyle = TextStyle.Default
        private set

    var overlineTextStyle: TextStyle = TextStyle.Default
        private set

    var supportingTextStyle: TextStyle = TextStyle.Default
        private set

    fun minHeight(height: Dp) {
        minHeight = height
    }

    fun contentPadding(padding: PaddingValues) {
        contentPadding = padding
    }

    fun twoLineMinHeight(height: Dp) {
        twoLineMinHeight = height
    }

    fun twoLineContentPadding(padding: PaddingValues) {
        twoLineContentPadding = padding
    }

    fun leadingGap(gap: Dp) {
        leadingGap = gap
    }

    fun trailingGap(gap: Dp) {
        trailingGap = gap
    }

    fun lineGap(gap: Dp) {
        lineGap = gap
    }

    fun groupShape(shape: CornerBasedShape) {
        groupShape = shape
    }

    fun dividerThickness(thickness: Dp) {
        dividerThickness = thickness
    }

    fun dividerColor(color: Color) {
        dividerColor = color
    }

    fun containerColor(color: Color) {
        containerColor = color
    }

    fun disabledContainerColor(color: Color) {
        disabledContainerColor = color
    }

    fun headlineColor(color: Color) {
        headlineColor = color
    }

    fun overlineColor(color: Color) {
        overlineColor = color
    }

    fun supportingColor(color: Color) {
        supportingColor = color
    }

    fun leadingColor(color: Color) {
        leadingColor = color
    }

    fun trailingColor(color: Color) {
        trailingColor = color
    }

    fun disabledContentColor(color: Color) {
        disabledContentColor = color
    }

    fun headlineTextStyle(style: TextStyle) {
        headlineTextStyle = style
    }

    fun overlineTextStyle(style: TextStyle) {
        overlineTextStyle = style
    }

    fun supportingTextStyle(style: TextStyle) {
        supportingTextStyle = style
    }
}

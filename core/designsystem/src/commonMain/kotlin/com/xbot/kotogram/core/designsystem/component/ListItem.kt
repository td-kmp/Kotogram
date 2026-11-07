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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.ListItemColors
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
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.ListItemDefaults as MaterialListItemDefaults

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
    colors: ListItemColors = ListItemDefaults.colors(),
    contentPadding: PaddingValues = ListItemDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null
) {
    val typography = KotogramTheme.typography
    val dividerColor = KotogramTheme.colors.outlineVariant
    val hasDivider = position == ListItemPosition.First || position == ListItemPosition.Middle
    var rowStart by remember { mutableFloatStateOf(0f) }
    var textStart by remember { mutableFloatStateOf(0f) }
    Row(
        modifier = modifier
            .clip(ListItemDefaults.shape(position))
            .background(if (enabled) colors.containerColor else colors.disabledContainerColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = KotogramTheme.indication,
                        enabled = enabled,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .onPlaced { rowStart = it.positionInRoot().x }
            // Rows of a group are split under their text, as in Telegram for macOS's settings.
            .drawBehind {
                if (hasDivider) {
                    val y = size.height - ListItemDefaults.DividerThickness.toPx() / 2
                    drawLine(
                        color = dividerColor,
                        start = Offset(textStart - rowStart, y),
                        end = Offset(size.width, y),
                        strokeWidth = ListItemDefaults.DividerThickness.toPx()
                    )
                }
            }
            .heightIn(min = ListItemDefaults.MinHeight)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingContent != null) {
            Box(Modifier.padding(end = ListItemDefaults.LeadingGap), contentAlignment = Alignment.Center) {
                ProvideContent(colors.leading(enabled), typography.bodyLarge, leadingContent)
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .onPlaced { textStart = it.positionInRoot().x },
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.LineGap)
        ) {
            if (overlineContent != null) {
                ProvideContent(colors.overline(enabled), typography.labelSmall, overlineContent)
            }
            ProvideContent(colors.headline(enabled), typography.bodyLarge, headlineContent)
            if (supportingContent != null) {
                ProvideContent(colors.supporting(enabled), typography.bodyMedium, supportingContent)
            }
        }
        if (trailingContent != null) {
            Box(Modifier.padding(start = ListItemDefaults.TrailingGap), contentAlignment = Alignment.Center) {
                ProvideContent(colors.trailing(enabled), typography.bodyLarge, trailingContent)
            }
        }
    }
}

object ListItemDefaults {
    val MinHeight: Dp = 44.dp

    val ContentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 12.dp)

    val LeadingGap: Dp = 12.dp

    val TrailingGap: Dp = 10.dp

    val LineGap: Dp = 2.dp

    val DividerThickness: Dp = 1.dp

    @Composable
    fun colors(): ListItemColors = MaterialListItemDefaults.colors()

    @Composable
    fun shape(position: ListItemPosition?): Shape {
        val shape = KotogramTheme.shapes.medium
        return when (position) {
            null -> RectangleShape
            ListItemPosition.Single -> shape
            ListItemPosition.First -> shape.copy(bottomStart = ZeroCornerSize, bottomEnd = ZeroCornerSize)
            ListItemPosition.Middle -> RectangleShape
            ListItemPosition.Last -> shape.copy(topStart = ZeroCornerSize, topEnd = ZeroCornerSize)
        }
    }
}

@Composable
private fun ProvideContent(color: Color, style: TextStyle, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalContentColor provides color,
        LocalTextStyle provides LocalTextStyle.current.merge(style),
        content = content
    )
}

private fun ListItemColors.headline(enabled: Boolean) = if (enabled) contentColor else disabledContentColor

private fun ListItemColors.leading(enabled: Boolean) = if (enabled) leadingContentColor else disabledLeadingContentColor

private fun ListItemColors.trailing(enabled: Boolean) =
    if (enabled) trailingContentColor else disabledTrailingContentColor

private fun ListItemColors.overline(enabled: Boolean) =
    if (enabled) overlineContentColor else disabledOverlineContentColor

private fun ListItemColors.supporting(enabled: Boolean) =
    if (enabled) supportingContentColor else disabledSupportingContentColor

@KotogramPreview
@Composable
private fun ListItemPreview() {
    Column(Modifier.padding(KotogramTheme.spacing.large)) {
        ListItem(
            headlineContent = { Text("Notifications") },
            leadingContent = { MaterialIcon(KotogramIcons.Bell, contentDescription = null) },
            trailingContent = { MaterialIcon(KotogramIcons.ChevronRight, contentDescription = null) },
            onClick = {},
            position = ListItemPosition.First
        )
        ListItem(
            headlineContent = { Text("Language") },
            supportingContent = { Text("English") },
            leadingContent = { MaterialIcon(KotogramIcons.Globe, contentDescription = null) },
            onClick = {},
            position = ListItemPosition.Middle
        )
        ListItem(
            headlineContent = { Text("Sync contacts") },
            trailingContent = { Switch(checked = true, onCheckedChange = null) },
            position = ListItemPosition.Last
        )
    }
}

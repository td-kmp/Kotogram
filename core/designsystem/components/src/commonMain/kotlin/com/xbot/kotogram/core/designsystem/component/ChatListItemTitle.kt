package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.SemanticsModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.extensions.shownLineRight
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun ChatListItemScope.Title(
    text: String,
    modifier: Modifier = Modifier,
    mark: TitleMark? = null,
    muted: Boolean = false,
    secret: Boolean = false,
    label: String? = null,
    emojiStatus: @Composable (() -> Unit)? = null,
    botVerification: @Composable (() -> Unit)? = null,
) {
    val titleStyle = if (secret) texts.title.merge(color = style.secretColor) else texts.title
    val measurer = rememberTextMeasurer()
    Layout(
        modifier = modifier,
        content = {
            Spacer(Modifier.layoutId(TitlePart.Text).titleText(text, titleStyle, measurer, texts.ellipsisWidth))
            if (botVerification != null) {
                Box(Modifier.layoutId(TitlePart.BotVerification).size(BotVerificationSize), Alignment.Center) {
                    botVerification()
                }
            }
            if (secret) {
                Icon(
                    imageVector = KotogramIcons.SecretLock,
                    contentDescription = null,
                    modifier = Modifier.layoutId(TitlePart.Secret),
                    tint = style.secretColor,
                )
            }
            if (emojiStatus != null) {
                Box(Modifier.layoutId(TitlePart.EmojiStatus).size(style.emojiStatusSize), Alignment.Center) {
                    emojiStatus()
                }
            }
            if (mark != null) TitleMarkIcon(mark, Modifier.layoutId(TitlePart.Mark))
            if (muted) {
                Icon(
                    imageVector = KotogramIcons.SpeakerOff,
                    contentDescription = null,
                    modifier = Modifier.layoutId(TitlePart.Muted).size(style.mutedIconSize),
                    tint = style.iconColor,
                )
            }
            if (label != null) TitleLabel(label, Modifier.layoutId(TitlePart.Label))
        },
        measurePolicy = remember(style, texts, mark) { TitleMeasurePolicy(style, texts.title.fontSize, mark) },
    )
}

private fun Modifier.titleText(
    title: String,
    style: TextStyle,
    measurer: TextMeasurer,
    ellipsis: Float,
): Modifier = this then TitleTextElement(title, style, measurer, ellipsis)

private data class TitleTextElement(
    val title: String,
    val style: TextStyle,
    val measurer: TextMeasurer,
    val ellipsis: Float,
) : ModifierNodeElement<TitleTextNode>() {
    override fun create() = TitleTextNode(title, style, measurer, ellipsis)

    override fun update(node: TitleTextNode) {
        node.title = title
        node.style = style
        node.measurer = measurer
        node.ellipsis = ellipsis
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "titleText"
        properties["title"] = title
    }
}

private class TitleTextNode(
    var title: String,
    var style: TextStyle,
    var measurer: TextMeasurer,
    var ellipsis: Float,
) : Modifier.Node(),
    LayoutModifierNode,
    DrawModifierNode,
    SemanticsModifierNode {
    private var textLayout: TextLayoutResult? = null

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val result = measurer.measure(
            text = title,
            style = style,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            constraints = constraints,
            layoutDirection = layoutDirection,
            density = this,
        )
        if (result != textLayout) {
            textLayout = result
            invalidateDraw()
        }
        val (width, height) = result.size
        val end = ceil(result.shownLineRight(0, ellipsis)).toInt()
        val placeable = measurable.measure(Constraints.fixed(width, height))
        val lines = mapOf(FirstBaseline to result.firstBaseline.roundToInt(), TitleTextEnd to end)
        return layout(width, height, lines) { placeable.place(0, 0) }
    }

    override fun ContentDrawScope.draw() {
        textLayout?.let { drawText(it) }
        drawContent()
    }

    override fun SemanticsPropertyReceiver.applySemantics() {
        text = AnnotatedString(title)
    }
}

private val TitleTextEnd = VerticalAlignmentLine(::maxOf)

@Composable
private fun ChatListItemScope.TitleMarkIcon(
    mark: TitleMark,
    modifier: Modifier = Modifier,
) {
    when (mark) {
        TitleMark.Verified -> VerifiedBadge(
            style.verifiedColor,
            style.verifiedContentColor,
            modifier.size(style.verifiedSize),
        )

        TitleMark.Premium -> Box(modifier.size(style.emojiStatusSize), Alignment.Center) {
            Icon(
                imageVector = KotogramIcons.PremiumStar,
                contentDescription = null,
                modifier = Modifier.size(style.emojiStatusSize * PREMIUM_STAR_SCALE),
                tint = style.premiumColor,
            )
        }

        is TitleMark.Warning -> Box(
            modifier = modifier
                .height(WarningHeight)
                .border(WarningBorder, style.scamColor, RoundedCornerShape(WarningCorner))
                .padding(horizontal = WarningPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = mark.label,
                style = texts.smallLabel.merge(color = style.scamColor),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ChatListItemScope.TitleLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    val background = style.labelColor.copy(alpha = style.labelColor.alpha * LABEL_BACKGROUND)
    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(LabelCorner))
            .padding(horizontal = LabelPaddingX, vertical = LabelPaddingY),
    ) {
        Text(
            text = text,
            style = texts.smallLabel.merge(color = style.labelColor),
            maxLines = 1,
        )
    }
}

private enum class TitlePart { Text, BotVerification, Secret, EmojiStatus, Mark, Muted, Label }

private class TitleMeasurePolicy(
    private val style: ChatListItemStyleScope,
    private val fontSize: TextUnit,
    private val mark: TitleMark?,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val parts = measurables.associateBy { it.layoutId as TitlePart }
        val icons = parts.filterKeys { it != TitlePart.Text }.mapValues { (_, it) -> it.measure(Constraints()) }
        val leading = if (icons[TitlePart.BotVerification] != null) BotVerificationOffset.roundToPx() else 0
        val textStart = leading + (icons[TitlePart.Secret]?.let { it.width + SecretGap.roundToPx() } ?: 0)
        val labelReserve = icons[TitlePart.Label]?.let { it.width + LabelReserve.roundToPx() } ?: 0
        val reserve = iconsReserve(icons) + leading + labelReserve
        val title = parts.getValue(TitlePart.Text).measure(
            Constraints(maxWidth = (constraints.maxWidth - reserve).coerceAtLeast(0)),
        )
        val baseline = title[FirstBaseline].takeIf { it != AlignmentLine.Unspecified } ?: title.height
        val textEnd = textStart + (title[TitleTextEnd].takeIf { it != AlignmentLine.Unspecified } ?: title.width)
        val placed = leadingIcons(icons, leading, baseline) + trailingIcons(icons, textEnd, baseline)
        val width = maxOf(textStart + title.width, placed.maxOfOrNull { it.x + it.placeable.width } ?: 0)
        return layout(width.coerceAtMost(constraints.maxWidth), title.height) {
            title.place(textStart, 0)
            placed.forEach { it.placeable.place(it.x, it.y) }
        }
    }

    private fun MeasureScope.centredOnLine(
        icon: Placeable,
        baseline: Int,
        lift: Float,
    ): Int = baseline - (fontSize.toPx() * lift).roundToInt() - icon.height / 2

    private fun MeasureScope.leadingIcons(
        icons: Map<TitlePart, Placeable>,
        leading: Int,
        baseline: Int,
    ): List<PlacedIcon> = listOfNotNull(
        icons[TitlePart.BotVerification]?.let { PlacedIcon(it, 0, centredOnLine(it, baseline, TITLE_ICON_LIFT)) },
        icons[TitlePart.Secret]?.let { PlacedIcon(it, leading, centredOnLine(it, baseline, SECRET_ICON_LIFT)) },
    )

    private fun MeasureScope.trailingIcons(
        icons: Map<TitlePart, Placeable>,
        textEnd: Int,
        baseline: Int,
    ): List<PlacedIcon> {
        var origin = textEnd + TitleIconStart.roundToPx()
        val placed = mutableListOf<PlacedIcon>()
        for (part in listOf(TitlePart.EmojiStatus, TitlePart.Mark, TitlePart.Muted)) {
            val icon = icons[part] ?: continue
            val muted = part == TitlePart.Muted
            val x = if (muted) origin + MutedIconOffset.roundToPx() else origin
            placed +=
                PlacedIcon(icon, x, centredOnLine(icon, baseline, if (muted) MUTED_ICON_LIFT else TITLE_ICON_LIFT))
            origin += icon.width + if (muted) MutedIconGap.roundToPx() else TitleIconGap.roundToPx()
        }
        icons[TitlePart.Label]?.let {
            val x = origin + if (icons[TitlePart.Muted] == null) LabelStart.roundToPx() else 0
            placed += PlacedIcon(it, x, centredOnLine(it, baseline, TITLE_ICON_LIFT))
        }
        return placed
    }

    private fun MeasureScope.iconsReserve(placeables: Map<TitlePart, Placeable>): Int {
        var reserve = 0
        fun add(width: Int) {
            val gap = if (reserve == 0) TitleIconsFirstReserve else TitleIconsNextReserve
            reserve += gap.roundToPx() + width
        }
        if (placeables[TitlePart.Muted] != null) add(style.mutedIconSize.roundToPx())
        placeables[TitlePart.Secret]?.let { reserve += it.width + SecretReserve.roundToPx() }
        placeables[TitlePart.Mark]?.let { icon ->
            add(if (mark is TitleMark.Warning) icon.width else MarkReserve.roundToPx())
        }
        if (placeables[TitlePart.EmojiStatus] != null) add(EmojiStatusReserve.roundToPx())
        return reserve
    }
}

private class PlacedIcon(
    val placeable: Placeable,
    val x: Int,
    val y: Int,
)

@Composable
private fun VerifiedBadge(
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        Icon(KotogramIcons.VerifiedBadge, null, Modifier.fillMaxSize(), tint = color)
        Icon(KotogramIcons.VerifiedCheck, null, Modifier.fillMaxSize(), tint = contentColor)
    }
}

private val TitleIconStart = 3.dp

private val TitleIconGap = 4.dp

private const val TITLE_ICON_LIFT = 1f / 3

private const val MUTED_ICON_LIFT = 7f / 24

private val TitleIconsFirstReserve = 4.dp

private val TitleIconsNextReserve = 2.dp

private val MarkReserve = 8.dp

private val EmojiStatusReserve = 16.dp

private val MutedIconOffset = (-5).dp

private val MutedIconGap = 1.dp

private val BotVerificationSize = 16.dp

private val BotVerificationOffset = 19.dp

private val SecretGap = 3.dp

private val SecretReserve = 2.dp

private const val SECRET_ICON_LIFT = 6f / 16

private const val PREMIUM_STAR_SCALE = 36f / 44

private val WarningHeight = 16.dp

private val WarningBorder = 1.dp

private val WarningCorner = 2.dp

private val WarningPadding = 4.5.dp

private val LabelReserve = 8.dp

private val LabelStart = 7.dp

private val LabelCorner = 4.dp

private val LabelPaddingX = 4.dp

private val LabelPaddingY = 1.5.dp

private const val LABEL_BACKGROUND = 0.125f

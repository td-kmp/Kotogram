package com.xbot.kotogram.feature.chats.impl.component

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.TextUnit
import com.xbot.kotogram.core.designsystem.component.LottieImage
import com.xbot.kotogram.core.designsystem.component.NetworkImage
import com.xbot.kotogram.core.designsystem.component.NetworkImageStyle
import com.xbot.kotogram.core.designsystem.component.SPOILER_ANNOTATION
import io.xbot.tdlib.api.FormattedText
import io.xbot.tdlib.api.Sticker
import io.xbot.tdlib.api.StickerFormatTgs
import io.xbot.tdlib.api.StickerFormatWebp
import io.xbot.tdlib.api.TextEntityTypeCustomEmoji
import io.xbot.tdlib.api.TextEntityTypeSpoiler

@Composable
internal fun CustomEmoji(
    sticker: Sticker?,
    fallback: String,
    modifier: Modifier = Modifier,
    loops: Int = Int.MAX_VALUE,
) {
    when {
        sticker == null -> Text(fallback, modifier)

        sticker.format is StickerFormatTgs -> LottieImage(
            sticker.sticker,
            modifier,
            iterations = loops,
        )

        sticker.format is StickerFormatWebp ->
            NetworkImage(
                sticker.sticker,
                null,
                modifier,
                contentScale = ContentScale.Fit,
                style = TransparentImage,
            )

        else -> NetworkImage(
            model = sticker.thumbnail?.file,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit,
            style = TransparentImage,
        )
    }
}

private val TransparentImage = NetworkImageStyle { placeholderColor(Color.Transparent) }

internal fun FormattedText.toPreviewString(): AnnotatedString = buildAnnotatedString {
    val flat = text.replace('\n', ' ')
    var cursor = 0
    entities
        .filter { it.type is TextEntityTypeCustomEmoji && it.offset >= cursor }
        .sortedBy { it.offset }
        .forEach { entity ->
            val start = entity.offset.coerceIn(cursor, flat.length)
            val end = (entity.offset + entity.length).coerceIn(start, flat.length)
            append(flat.substring(cursor, start))
            val id = (entity.type as TextEntityTypeCustomEmoji).customEmojiId
            appendInlineContent(customEmojiSlot(id), flat.substring(start, end))
            cursor = end
        }
    append(flat.substring(cursor))
    entities.forEach { entity ->
        val start = entity.offset.coerceIn(0, flat.length)
        val end = (entity.offset + entity.length).coerceIn(start, flat.length)
        if (entity.type is TextEntityTypeSpoiler && start < end) addStringAnnotation(SPOILER_ANNOTATION, "", start, end)
    }
}

internal fun customEmojiSlot(id: Long) = "custom_emoji:$id"

internal fun customEmojiContent(
    stickers: Map<Long, Sticker>,
    ids: Set<Long>,
    size: TextUnit,
): Map<String, InlineTextContent> = ids.associate { id ->
    customEmojiSlot(id) to
        InlineTextContent(
            Placeholder(size, size, PlaceholderVerticalAlign.TextCenter),
        ) { alternate ->
            CustomEmoji(stickers[id], alternate)
        }
}

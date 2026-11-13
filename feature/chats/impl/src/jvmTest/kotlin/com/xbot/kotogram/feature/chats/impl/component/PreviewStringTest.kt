package com.xbot.kotogram.feature.chats.impl.component

import com.xbot.kotogram.core.designsystem.component.SPOILER_ANNOTATION
import io.xbot.tdlib.api.FormattedText
import io.xbot.tdlib.api.TextEntity
import io.xbot.tdlib.api.TextEntityTypeBold
import io.xbot.tdlib.api.TextEntityTypeSpoiler
import kotlin.test.Test
import kotlin.test.assertEquals

class PreviewStringTest {
    @Test
    fun `a spoiler entity is marked over its range`() {
        val text = FormattedText(
            text = "The answer is forty-two",
            entities = [
                TextEntity(offset = 0, length = 3, type = TextEntityTypeBold),
                TextEntity(offset = 14, length = 9, type = TextEntityTypeSpoiler),
            ],
        )

        val spoilers = text.toPreviewString().getStringAnnotations(SPOILER_ANNOTATION, 0, text.text.length)

        assertEquals([14 to 23], spoilers.map { it.start to it.end })
    }

    @Test
    fun `a spoiler past the end of the text is cut to it`() {
        val text = FormattedText(
            text = "secret",
            entities = [TextEntity(offset = 3, length = 30, type = TextEntityTypeSpoiler)],
        )

        val spoilers = text.toPreviewString().getStringAnnotations(SPOILER_ANNOTATION, 0, text.text.length)

        assertEquals([3 to 6], spoilers.map { it.start to it.end })
    }
}

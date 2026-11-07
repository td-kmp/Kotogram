package com.xbot.kotogram.catalog.pages

import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.component.RichText
import io.xbot.tdlib.api.FormattedText
import io.xbot.tdlib.api.TextEntity
import io.xbot.tdlib.api.TextEntityType
import io.xbot.tdlib.api.TextEntityTypeBold
import io.xbot.tdlib.api.TextEntityTypeCode
import io.xbot.tdlib.api.TextEntityTypeEmailAddress
import io.xbot.tdlib.api.TextEntityTypeItalic
import io.xbot.tdlib.api.TextEntityTypeMention
import io.xbot.tdlib.api.TextEntityTypeStrikethrough
import io.xbot.tdlib.api.TextEntityTypeTextUrl
import io.xbot.tdlib.api.TextEntityTypeUnderline

internal val RichTextPage = CatalogPage(
    id = "rich-text",
    section = CatalogSection.Components,
    name = "RichText",
    description = "TDLib's FormattedText as Compose text: core:resources turns its entities into an AnnotatedString, " +
        "the design system styles it from the theme. Links open, mentions and hashtags call back.",
    examples = listOf(
        CatalogExample("Terms of Service") {
            RichText(
                formatted(
                    "By signing up, you agree to the Terms of Service.",
                    "Terms of Service" to TextEntityTypeTextUrl("https://telegram.org/tos")
                )
            )
        },
        CatalogExample("Every inline style") {
            RichText(
                formatted(
                    "Bold, italic, underlined, struck out, code, @telegram and help@telegram.org.",
                    "Bold" to TextEntityTypeBold,
                    "italic" to TextEntityTypeItalic,
                    "underlined" to TextEntityTypeUnderline,
                    "struck out" to TextEntityTypeStrikethrough,
                    "code" to TextEntityTypeCode,
                    "@telegram" to TextEntityTypeMention,
                    "help@telegram.org" to TextEntityTypeEmailAddress
                ),
                onEntityClick = {}
            )
        }
    )
)

private fun formatted(text: String, vararg entities: Pair<String, TextEntityType>) = FormattedText(
    text = text,
    entities = entities.map { (part, type) -> TextEntity(text.indexOf(part), part.length, type) }
)

package com.xbot.kotogram.core.tdlib.mapper

import com.xbot.kotogram.core.domain.model.DateTimeFormattingType
import com.xbot.kotogram.core.domain.model.DateTimePartPrecision
import com.xbot.kotogram.core.domain.model.FormattedText
import com.xbot.kotogram.core.domain.model.TextEntity
import com.xbot.kotogram.core.domain.model.TextEntityType
import io.xbot.tdlib.api.DateTimeFormattingType as TdDateTimeFormattingType
import io.xbot.tdlib.api.DateTimeFormattingTypeAbsolute
import io.xbot.tdlib.api.DateTimeFormattingTypeRelative
import io.xbot.tdlib.api.DateTimePartPrecision as TdDateTimePartPrecision
import io.xbot.tdlib.api.DateTimePartPrecisionLong
import io.xbot.tdlib.api.DateTimePartPrecisionNone
import io.xbot.tdlib.api.DateTimePartPrecisionShort
import io.xbot.tdlib.api.FormattedText as TdFormattedText
import io.xbot.tdlib.api.TextEntity as TdTextEntity
import io.xbot.tdlib.api.TextEntityType as TdTextEntityType
import io.xbot.tdlib.api.TextEntityTypeBankCardNumber
import io.xbot.tdlib.api.TextEntityTypeBlockQuote
import io.xbot.tdlib.api.TextEntityTypeBold
import io.xbot.tdlib.api.TextEntityTypeBotCommand
import io.xbot.tdlib.api.TextEntityTypeCashtag
import io.xbot.tdlib.api.TextEntityTypeCode
import io.xbot.tdlib.api.TextEntityTypeCustomEmoji
import io.xbot.tdlib.api.TextEntityTypeDateTime
import io.xbot.tdlib.api.TextEntityTypeEmailAddress
import io.xbot.tdlib.api.TextEntityTypeExpandableBlockQuote
import io.xbot.tdlib.api.TextEntityTypeHashtag
import io.xbot.tdlib.api.TextEntityTypeItalic
import io.xbot.tdlib.api.TextEntityTypeMediaTimestamp
import io.xbot.tdlib.api.TextEntityTypeMention
import io.xbot.tdlib.api.TextEntityTypeMentionName
import io.xbot.tdlib.api.TextEntityTypePhoneNumber
import io.xbot.tdlib.api.TextEntityTypePre
import io.xbot.tdlib.api.TextEntityTypePreCode
import io.xbot.tdlib.api.TextEntityTypeSpoiler
import io.xbot.tdlib.api.TextEntityTypeStrikethrough
import io.xbot.tdlib.api.TextEntityTypeTextUrl
import io.xbot.tdlib.api.TextEntityTypeTonAddress
import io.xbot.tdlib.api.TextEntityTypeUnderline
import io.xbot.tdlib.api.TextEntityTypeUrl
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun TdFormattedText.toDomain() = FormattedText(
    text = text,
    entities = entities.map {
        it.toDomain()
    }
)

private fun TdTextEntity.toDomain() =
    TextEntity(offset = offset, length = length, type = type.required().toDomain())

// One branch per TDLib entity type: a 1:1 mapping of a sealed type can't be split.
@Suppress("CyclomaticComplexMethod")
@OptIn(ExperimentalTime::class)
private fun TdTextEntityType.toDomain(): TextEntityType = when (this) {
    TextEntityTypeMention -> TextEntityType.Mention

    TextEntityTypeHashtag -> TextEntityType.Hashtag

    TextEntityTypeCashtag -> TextEntityType.Cashtag

    TextEntityTypeBotCommand -> TextEntityType.BotCommand

    TextEntityTypeUrl -> TextEntityType.Url

    TextEntityTypeEmailAddress -> TextEntityType.EmailAddress

    TextEntityTypePhoneNumber -> TextEntityType.PhoneNumber

    TextEntityTypeBankCardNumber -> TextEntityType.BankCardNumber

    TextEntityTypeTonAddress -> TextEntityType.TonAddress

    TextEntityTypeBold -> TextEntityType.Bold

    TextEntityTypeItalic -> TextEntityType.Italic

    TextEntityTypeUnderline -> TextEntityType.Underline

    TextEntityTypeStrikethrough -> TextEntityType.Strikethrough

    TextEntityTypeSpoiler -> TextEntityType.Spoiler

    TextEntityTypeCode -> TextEntityType.Code

    TextEntityTypePre -> TextEntityType.Pre

    is TextEntityTypePreCode -> TextEntityType.PreCode(language)

    TextEntityTypeBlockQuote -> TextEntityType.BlockQuote

    TextEntityTypeExpandableBlockQuote -> TextEntityType.ExpandableBlockQuote

    is TextEntityTypeTextUrl -> TextEntityType.TextUrl(url)

    is TextEntityTypeMentionName -> TextEntityType.MentionName(userId)

    is TextEntityTypeCustomEmoji -> TextEntityType.CustomEmoji(customEmojiId)

    is TextEntityTypeMediaTimestamp -> TextEntityType.MediaTimestamp(mediaTimestamp.seconds)

    is TextEntityTypeDateTime -> TextEntityType.DateTime(
        unixTime = Instant.fromEpochSeconds(unixTime.toLong()),
        formattingType = formattingType.required().toDomain()
    )
}

private fun TdDateTimeFormattingType.toDomain(): DateTimeFormattingType = when (this) {
    DateTimeFormattingTypeRelative -> DateTimeFormattingType.Relative

    is DateTimeFormattingTypeAbsolute -> DateTimeFormattingType.Absolute(
        timePrecision = timePrecision.required().toDomain(),
        datePrecision = datePrecision.required().toDomain(),
        showDayOfWeek = showDayOfWeek
    )
}

private fun TdDateTimePartPrecision.toDomain(): DateTimePartPrecision = when (this) {
    DateTimePartPrecisionNone -> DateTimePartPrecision.None
    DateTimePartPrecisionShort -> DateTimePartPrecision.Short
    DateTimePartPrecisionLong -> DateTimePartPrecision.Long
}

/** Generated TDLib classes make nested objects nullable with a default; TDLib always sends the required ones. */
internal fun <T : Any> T?.required(): T = checkNotNull(this) { "TDLib omitted a required field" }

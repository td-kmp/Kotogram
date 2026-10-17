package com.xbot.kotogram.core.domain.model

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** TDLib's `FormattedText`, field for field. */
data class FormattedText(val text: String, val entities: List<TextEntity>)

/** [offset] and [length] are in UTF-16 code units, as in TDLib. */
data class TextEntity(val offset: Int, val length: Int, val type: TextEntityType)

sealed interface TextEntityType {
    data object Mention : TextEntityType

    data object Hashtag : TextEntityType

    data object Cashtag : TextEntityType

    data object BotCommand : TextEntityType

    data object Url : TextEntityType

    data object EmailAddress : TextEntityType

    data object PhoneNumber : TextEntityType

    data object BankCardNumber : TextEntityType

    data object TonAddress : TextEntityType

    data object Bold : TextEntityType

    data object Italic : TextEntityType

    data object Underline : TextEntityType

    data object Strikethrough : TextEntityType

    data object Spoiler : TextEntityType

    data object Code : TextEntityType

    data object Pre : TextEntityType

    data class PreCode(val language: String) : TextEntityType

    data object BlockQuote : TextEntityType

    data object ExpandableBlockQuote : TextEntityType

    data class TextUrl(val url: String) : TextEntityType

    data class MentionName(val userId: Long) : TextEntityType

    data class CustomEmoji(val customEmojiId: Long) : TextEntityType

    data class MediaTimestamp(val mediaTimestamp: Duration) : TextEntityType

    @OptIn(ExperimentalTime::class)
    data class DateTime(val unixTime: Instant, val formattingType: DateTimeFormattingType) :
        TextEntityType
}

sealed interface DateTimeFormattingType {
    data object Relative : DateTimeFormattingType

    data class Absolute(
        val timePrecision: DateTimePartPrecision,
        val datePrecision: DateTimePartPrecision,
        val showDayOfWeek: Boolean
    ) : DateTimeFormattingType
}

enum class DateTimePartPrecision { None, Short, Long }

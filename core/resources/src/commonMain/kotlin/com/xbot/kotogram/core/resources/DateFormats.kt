package com.xbot.kotogram.core.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

interface DateFormats {
    fun time(epochSeconds: Long): String

    fun weekday(epochSeconds: Long): String

    fun dayMonth(epochSeconds: Long): String

    fun dayMonthYear(epochSeconds: Long): String
}

@Composable
fun rememberDateFormats(): DateFormats {
    val language = LocalLocalization.current.language?.pluralCode ?: BUNDLED_LANGUAGE
    val hour24 = platformHour24()
    return remember(language, hour24) { sharedDateFormats(DateFormatsKey(language, platformRegion(), hour24)) }
}

private data class DateFormatsKey(
    val language: String,
    val region: String?,
    val hour24: Boolean?,
)

private var shared: Pair<DateFormatsKey, DateFormats>? = null

private fun sharedDateFormats(key: DateFormatsKey): DateFormats = shared?.takeIf { it.first == key }?.second
    ?: platformDateFormats(key.language, key.region, key.hour24).also { shared = key to it }

@Composable
fun relativeDateString(epochSeconds: Long): String = rememberDateFormats().relative(epochSeconds)

fun DateFormats.relative(
    epochSeconds: Long,
    now: Instant = Clock.System.now(),
    zone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val date = Instant.fromEpochSeconds(epochSeconds).toLocalDateTime(zone).date
    val today = now.toLocalDateTime(zone).date
    val days = date.dayOfYear - today.dayOfYear
    return when {
        date.year != today.year -> dayMonthYear(epochSeconds)
        days == 0 -> time(epochSeconds)
        days > -WEEK -> weekday(epochSeconds)
        else -> dayMonth(epochSeconds)
    }
}

internal expect fun platformDateFormats(
    language: String,
    region: String?,
    hour24: Boolean?,
): DateFormats

internal expect fun platformRegion(): String?

@Composable
internal expect fun platformHour24(): Boolean?

internal fun timeSkeleton(hour24: Boolean?): String = when (hour24) {
    true -> "Hm"
    false -> "hm"
    null -> "jm"
}

internal const val WEEKDAY_SKELETON = "E"
internal const val DAY_MONTH_SKELETON = "Md"
internal const val DAY_MONTH_YEAR_SKELETON = "yyMd"

private const val BUNDLED_LANGUAGE = "en"
private const val WEEK = 7

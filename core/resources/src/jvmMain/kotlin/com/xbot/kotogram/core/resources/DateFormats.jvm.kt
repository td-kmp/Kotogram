package com.xbot.kotogram.core.resources

import androidx.compose.runtime.Composable
import java.time.Instant
import java.time.ZoneId
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

internal actual fun platformDateFormats(
    language: String,
    region: String?,
    hour24: Boolean?,
): DateFormats = JvmDateFormats(Locale.forLanguageTag(if (region != null) "$language-$region" else language), hour24)

internal actual fun platformRegion(): String? = Locale.getDefault().country.ifEmpty { null }

@Composable
internal actual fun platformHour24(): Boolean? = null

private class JvmDateFormats(
    private val locale: Locale,
    hour24: Boolean?,
) : DateFormats {
    private val time = formatter(timeSkeleton(hour24))
    private val weekday = formatter(WEEKDAY_SKELETON)
    private val dayMonth = formatter(DAY_MONTH_SKELETON)

    // Workaround: java.time resolves only CLDR's own skeletons (yMd, not yyMd); Telegram shows two-digit years.
    private val dayMonthYear = DateTimeFormatter.ofPattern(
        DateTimeFormatterBuilder.getLocalizedDateTimePattern(
            "yMd",
            IsoChronology.INSTANCE,
            locale,
        ).replace(Years, "yy"),
        locale,
    )

    override fun time(epochSeconds: Long): String = time.format(epochSeconds.zoned())

    override fun weekday(epochSeconds: Long): String =
        weekday.format(epochSeconds.zoned()).replaceFirstChar { it.titlecase(locale) }

    override fun dayMonth(epochSeconds: Long): String = dayMonth.format(epochSeconds.zoned())

    override fun dayMonthYear(epochSeconds: Long): String = dayMonthYear.format(epochSeconds.zoned())

    private fun formatter(template: String) = DateTimeFormatter.ofLocalizedPattern(template).withLocale(locale)

    private fun Long.zoned() = Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault())
}

private val Years = Regex("y+")

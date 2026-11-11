package com.xbot.kotogram.core.resources

import android.icu.text.DateFormat
import android.icu.util.ULocale
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.Date
import java.util.Locale
import android.text.format.DateFormat as SystemDateFormat

internal actual fun platformDateFormats(
    language: String,
    region: String?,
    hour24: Boolean?,
): DateFormats = AndroidDateFormats(
    ULocale.forLanguageTag(
        if (region !=
            null
        ) "$language-$region" else language,
    ),
    hour24,
)

internal actual fun platformRegion(): String? = Locale.getDefault().country.ifEmpty { null }

@Composable
internal actual fun platformHour24(): Boolean? = SystemDateFormat.is24HourFormat(LocalContext.current)

private class AndroidDateFormats(
    private val locale: ULocale,
    hour24: Boolean?,
) : DateFormats {
    private val time = DateFormat.getInstanceForSkeleton(timeSkeleton(hour24), locale)
    private val weekday = DateFormat.getInstanceForSkeleton(WEEKDAY_SKELETON, locale)
    private val dayMonth = DateFormat.getInstanceForSkeleton(DAY_MONTH_SKELETON, locale)
    private val dayMonthYear = DateFormat.getInstanceForSkeleton(DAY_MONTH_YEAR_SKELETON, locale)

    override fun time(epochSeconds: Long): String = time.format(epochSeconds.date())

    override fun weekday(epochSeconds: Long): String =
        weekday.format(epochSeconds.date()).replaceFirstChar { it.titlecase(locale.toLocale()) }

    override fun dayMonth(epochSeconds: Long): String = dayMonth.format(epochSeconds.date())

    override fun dayMonthYear(epochSeconds: Long): String = dayMonthYear.format(epochSeconds.date())

    private fun Long.date() = Date(this * MILLIS)
}

private const val MILLIS = 1000L

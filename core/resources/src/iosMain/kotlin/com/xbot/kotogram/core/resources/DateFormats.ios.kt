package com.xbot.kotogram.core.resources

import androidx.compose.runtime.Composable
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970

internal actual fun platformDateFormats(
    language: String,
    region: String?,
    hour24: Boolean?,
): DateFormats = IosDateFormats(NSLocale(if (region != null) "${language}_$region" else language), hour24)

internal actual fun platformRegion(): String? = NSLocale.currentLocale.countryCode

@Composable
internal actual fun platformHour24(): Boolean? =
    NSDateFormatter.dateFormatFromTemplate("j", 0u, NSLocale.currentLocale)?.let { 'a' !in it }

private class IosDateFormats(
    private val locale: NSLocale,
    hour24: Boolean?,
) : DateFormats {
    private val time = formatter(timeSkeleton(hour24))
    private val weekday = formatter(WEEKDAY_SKELETON)
    private val dayMonth = formatter(DAY_MONTH_SKELETON)
    private val dayMonthYear = formatter(DAY_MONTH_YEAR_SKELETON)

    override fun time(epochSeconds: Long): String = time.stringFromDate(epochSeconds.date())

    override fun weekday(epochSeconds: Long): String =
        weekday.stringFromDate(epochSeconds.date()).replaceFirstChar { it.titlecase() }

    override fun dayMonth(epochSeconds: Long): String = dayMonth.stringFromDate(epochSeconds.date())

    override fun dayMonthYear(epochSeconds: Long): String = dayMonthYear.stringFromDate(epochSeconds.date())

    private fun formatter(template: String) = NSDateFormatter().apply {
        locale = this@IosDateFormats.locale
        setLocalizedDateFormatFromTemplate(template)
    }

    private fun Long.date() = NSDate.dateWithTimeIntervalSince1970(toDouble())
}

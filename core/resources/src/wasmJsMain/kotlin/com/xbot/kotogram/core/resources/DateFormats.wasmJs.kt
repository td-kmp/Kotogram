// Workaround: js() bodies read their parameters by name, which detekt can't see.
@file:Suppress("UnusedParameter")

package com.xbot.kotogram.core.resources

import androidx.compose.runtime.Composable

internal actual fun platformDateFormats(
    language: String,
    region: String?,
    hour24: Boolean?,
): DateFormats = WebDateFormats(if (region != null) "$language-$region" else language, hour24)

internal actual fun platformRegion(): String? = browserLanguage().substringAfter('-', "").ifEmpty { null }

@Composable
internal actual fun platformHour24(): Boolean? = when (browserHourCycle()) {
    "h23", "h24" -> true
    "h11", "h12" -> false
    else -> null
}

private class WebDateFormats(
    private val tag: String,
    private val hour24: Boolean?,
) : DateFormats {
    override fun time(epochSeconds: Long): String = when (hour24) {
        true -> formatTime24(tag, epochSeconds.millis())
        false -> formatTime12(tag, epochSeconds.millis())
        null -> formatTime(tag, epochSeconds.millis())
    }

    override fun weekday(epochSeconds: Long): String =
        formatWeekday(tag, epochSeconds.millis()).replaceFirstChar { it.titlecase() }

    override fun dayMonth(epochSeconds: Long): String = formatDayMonth(tag, epochSeconds.millis())

    override fun dayMonthYear(epochSeconds: Long): String = formatDayMonthYear(tag, epochSeconds.millis())

    private fun Long.millis(): Double = this * MILLIS
}

private fun browserLanguage(): String = js("navigator.language")

private fun browserHourCycle(): String? =
    js("new Intl.DateTimeFormat(undefined, { hour: 'numeric' }).resolvedOptions().hourCycle ?? null")

private fun formatTime(
    tag: String,
    millis: Double,
): String = js("new Intl.DateTimeFormat(tag, { hour: 'numeric', minute: '2-digit' }).format(new Date(millis))")

private fun formatTime24(
    tag: String,
    millis: Double,
): String = js(
    "new Intl.DateTimeFormat(tag, { hour: 'numeric', minute: '2-digit', hourCycle: 'h23' }).format(new Date(millis))",
)

private fun formatTime12(
    tag: String,
    millis: Double,
): String = js(
    "new Intl.DateTimeFormat(tag, { hour: 'numeric', minute: '2-digit', hourCycle: 'h12' }).format(new Date(millis))",
)

private fun formatWeekday(
    tag: String,
    millis: Double,
): String = js("new Intl.DateTimeFormat(tag, { weekday: 'short' }).format(new Date(millis))")

private fun formatDayMonth(
    tag: String,
    millis: Double,
): String = js("new Intl.DateTimeFormat(tag, { day: 'numeric', month: 'numeric' }).format(new Date(millis))")

private fun formatDayMonthYear(
    tag: String,
    millis: Double,
): String =
    js("new Intl.DateTimeFormat(tag, { day: 'numeric', month: 'numeric', year: '2-digit' }).format(new Date(millis))")

private const val MILLIS = 1000.0

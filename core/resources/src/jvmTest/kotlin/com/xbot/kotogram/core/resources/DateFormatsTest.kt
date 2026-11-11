package com.xbot.kotogram.core.resources

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class DateFormatsTest {
    private val zone = TimeZone.currentSystemDefault()
    private val now = LocalDateTime(2026, 11, 12, 18, 30).toInstant(zone)

    @Test
    fun followsTelegramForIosRule() {
        val formats = platformDateFormats("ru", "RU", hour24 = true)
        assertEquals("12:05", formats.relative(at(2026, 11, 12), now, zone))
        assertEquals("Пн", formats.relative(at(2026, 11, 9), now, zone))
        assertEquals("Пт", formats.relative(at(2026, 11, 6), now, zone))
        assertEquals("05.11", formats.relative(at(2026, 11, 5), now, zone))
        assertEquals("13.10.25", formats.relative(at(2025, 10, 13), now, zone))
    }

    @Test
    fun takesTheRegionsOrderAndTheHourCycle() {
        val us = platformDateFormats("en", "US", hour24 = false)
        assertEquals("12:05 PM", us.relative(at(2026, 11, 12), now, zone).spaced())
        assertEquals("Mon", us.relative(at(2026, 11, 9), now, zone))
        assertEquals("10/13", us.relative(at(2026, 10, 13), now, zone))
        val britain = platformDateFormats("en", "GB", hour24 = true)
        assertEquals("12:05", britain.relative(at(2026, 11, 12), now, zone))
        assertEquals("13/10", britain.relative(at(2026, 10, 13), now, zone))
        assertEquals("13/10/25", britain.relative(at(2025, 10, 13), now, zone))
    }

    private fun at(
        year: Int,
        month: Int,
        day: Int,
    ): Long = LocalDateTime(year, month, day, 12, 5).toInstant(zone).epochSeconds

    private fun String.spaced() = replace(' ', ' ').replace(' ', ' ')
}

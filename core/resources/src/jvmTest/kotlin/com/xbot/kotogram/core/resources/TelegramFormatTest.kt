package com.xbot.kotogram.core.resources

import kotlin.test.Test
import kotlin.test.assertEquals

class TelegramFormatTest {
    @Test
    fun positionalArgumentsGoByTheirNumber() = assertEquals(
        "Bob invited Alice",
        "%2\$@ invited %1\$@".formatTelegram(arrayOf("Alice", "Bob"))
    )

    @Test
    fun bareArgumentsGoInOrder() = assertEquals(
        "3 of 5",
        "%d of %d".formatTelegram(arrayOf(3, 5))
    )

    @Test
    fun boldMarksAndEscapedPercentsAreDropped() = assertEquals(
        "Go to Settings, 100%",
        "Go to **Settings**, 100%%".formatTelegram(emptyArray())
    )

    @Test
    fun aMissingArgumentKeepsItsPlaceholder() = assertEquals(
        "Hello %@",
        "Hello %@".formatTelegram(emptyArray())
    )
}

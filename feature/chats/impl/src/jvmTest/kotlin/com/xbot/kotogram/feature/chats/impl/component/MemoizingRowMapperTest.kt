package com.xbot.kotogram.feature.chats.impl.component

import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class MemoizingRowMapperTest {
    private class Entry(
        val id: Long,
        val input: Any,
    )

    private class Row

    private val mapper = MemoizingRowMapper<Entry, Long, Row>(
        key = { it.id },
        hasSameInputs = { old, new -> old.input === new.input },
        toRow = { Row() },
    )

    @Test
    fun `unchanged entries give back the same list`() {
        val first = Any()
        val second = Any()

        val before = mapper.map(listOf(Entry(1, first), Entry(2, second)))
        val after = mapper.map(listOf(Entry(1, first), Entry(2, second)))

        assertSame(before, after)
    }

    @Test
    fun `a changed entry gets a new row and the others keep theirs`() {
        val first = Any()

        val before = mapper.map(listOf(Entry(1, first), Entry(2, Any())))
        val after = mapper.map(listOf(Entry(1, first), Entry(2, Any())))

        assertNotSame(before, after)
        assertSame(before[0], after[0])
        assertNotSame(before[1], after[1])
    }
}

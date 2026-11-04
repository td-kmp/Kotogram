package com.xbot.kotogram.core.data.store

import io.xbot.tdlib.api.Update
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.descriptors.elementNames

class UpdateCoverageTest {
    /** Decide what `reduce` does with a new type, then update `update-types.txt`. */
    @Test
    fun everyUpdateTypeOfThisTdlibVersionHasBeenConsidered() {
        val types = Update.serializer().descriptor.getElementDescriptor(
            1
        ).elementNames.toSortedSet()
        val known = checkNotNull(javaClass.getResource("/update-types.txt")).readText().lines()
            .filter { it.isNotBlank() }
            .toSortedSet()
        assertEquals(known, types)
    }
}

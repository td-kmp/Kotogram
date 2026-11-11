package com.xbot.kotogram.feature.chats.data.store

import io.xbot.tdlib.api.Update
import kotlinx.serialization.descriptors.elementNames
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateCoverageTest {
    @Test
    fun everyUpdateTypeOfThisTdlibVersionHasBeenConsidered() {
        val types = Update.serializer().descriptor.getElementDescriptor(1).elementNames.toSortedSet()
        val known = checkNotNull(javaClass.getResource("/update-types.txt")).readText().lines()
            .filter { it.isNotBlank() }
            .toSortedSet()
        assertEquals(known, types, "Decide what reduce does with the new types, then update update-types.txt.")
    }
}

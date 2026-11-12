package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PathExtTest {
    private val chipOverPill = Path().apply {
        addRectStack(listOf(Rect(0f, 0f, 60f, 20f), Rect(0f, 20f, 200f, 40f)), radius = 10f)
    }

    @Test
    fun `stacked rects make one outline as large as both`() {
        val bounds = chipOverPill.getBounds()

        assertEquals(0f, bounds.left, TOLERANCE)
        assertEquals(0f, bounds.top, TOLERANCE)
        assertEquals(200f, bounds.right, TOLERANCE)
        assertEquals(40f, bounds.bottom, TOLERANCE)
    }

    @Test
    fun `outer corners are cut and the step is filled in`() {
        assertTrue(chipOverPill.covers(30f, 10f))
        assertFalse(chipOverPill.covers(0.5f, 0.5f))
        assertFalse(chipOverPill.covers(150f, 5f))
        assertTrue(chipOverPill.covers(61f, 19f))
        assertFalse(chipOverPill.covers(69f, 11f))
    }

    @Test
    fun `edges closer than the snap are joined`() {
        val path = Path().apply {
            addRectStack(listOf(Rect(0f, 0f, 99f, 20f), Rect(0f, 20f, 100f, 40f)), radius = 4f, snap = 2f)
        }

        assertTrue(path.covers(99.5f, 10f))
    }

    private fun Path.covers(
        x: Float,
        y: Float,
    ): Boolean {
        val probe = Path().apply { addRect(Rect(Offset(x - PROBE / 2, y - PROBE / 2), Size(PROBE, PROBE))) }
        return !Path.combine(PathOperation.Intersect, this, probe).isEmpty
    }
}

private const val TOLERANCE = 0.01f
private const val PROBE = 0.2f

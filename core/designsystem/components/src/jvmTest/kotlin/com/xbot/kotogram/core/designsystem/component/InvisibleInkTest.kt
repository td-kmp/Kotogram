package com.xbot.kotogram.core.designsystem.component

import kotlin.test.Test
import kotlin.test.assertEquals

class InvisibleInkTest {
    private val seeds = 0 until SEEDS

    @Test
    fun `speckles drift as fast as CAEmitterLayer's`() {
        val speeds = seeds.map(::speckleSpeed).sorted()

        EmitterSpeeds.forEach { (share, speed) ->
            assertEquals(speed, speeds[(SEEDS * share).toInt()], SPEED_TOLERANCE, "at $share")
        }
    }

    @Test
    fun `half of the speckles are born dimmed, none under half`() {
        val born = seeds.map { speckleAlpha(it, age = 0f) }

        assertEquals(0.5f, born.count { it < 1f } / SEEDS.toFloat(), SHARE_TOLERANCE)
        assertEquals(0.5f, born.min(), SHARE_TOLERANCE)
    }

    @Test
    fun `a speckle keeps its alpha for a quarter of its life, lights up and fades`() {
        val seed = seeds.first { speckleAlpha(it, age = 0f) in 0.55f..0.6f }
        val born = speckleAlpha(seed, age = 0f)

        assertEquals(born, speckleAlpha(seed, age = 0.25f), ALPHA_TOLERANCE)
        assertEquals(1f, speckleAlpha(seed, age = 0.5f), ALPHA_TOLERANCE)
        assertEquals(born, speckleAlpha(seed, age = 0.75f), ALPHA_TOLERANCE)
        assertEquals(0f, speckleAlpha(seed, age = 0.95f), ALPHA_TOLERANCE)
    }
}

private val EmitterSpeeds = [0.1f to 0.65f, 0.25f to 1.63f, 0.5f to 3.54f, 0.75f to 5.78f, 0.9f to 7.55f]
private const val SEEDS = 20_000
private const val SPEED_TOLERANCE = 0.25f
private const val SHARE_TOLERANCE = 0.02f
private const val ALPHA_TOLERANCE = 0.001f

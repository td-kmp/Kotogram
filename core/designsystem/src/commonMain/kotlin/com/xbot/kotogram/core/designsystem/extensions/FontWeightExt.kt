package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.text.font.FontWeight
import kotlin.math.abs

internal fun FontWeight?.appleWeight(): Double {
    val weight = (this ?: FontWeight.Normal).weight
    return AppleWeights.minBy { (fontWeight, _) -> abs(fontWeight.weight - weight) }.second
}

@Suppress("MagicNumber")
private val AppleWeights = listOf(
    FontWeight.Thin to -0.8,
    FontWeight.ExtraLight to -0.6,
    FontWeight.Light to -0.4,
    FontWeight.Normal to 0.0,
    FontWeight.Medium to 0.23,
    FontWeight.SemiBold to 0.3,
    FontWeight.Bold to 0.4,
    FontWeight.ExtraBold to 0.56,
    FontWeight.Black to 0.62
)

package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

internal fun Color.isDark(): Boolean = luminance() < HALF_LUMINANCE

internal fun Color.isLightOn(background: Color): Boolean = luminance() > minOf(HALF_LUMINANCE, background.luminance())

private const val HALF_LUMINANCE = 0.5f

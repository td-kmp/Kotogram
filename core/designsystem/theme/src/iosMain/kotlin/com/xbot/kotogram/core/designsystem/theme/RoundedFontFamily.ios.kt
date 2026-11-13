package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.SystemFont

@OptIn(ExperimentalTextApi::class)
actual val platformRoundedFontFamily: FontFamily? = if (currentPlatform.isApple) {
    FontFamily(
        [FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold].map { weight ->
            SystemFont(APPLE_ROUNDED, weight)
        },
    )
} else {
    null
}

private const val APPLE_ROUNDED = ".AppleSystemUIFontRounded"

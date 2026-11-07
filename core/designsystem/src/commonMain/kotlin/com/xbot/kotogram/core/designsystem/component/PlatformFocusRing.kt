package com.xbot.kotogram.core.designsystem.component

// Phones draw a focus ring; desktop and web follow Telegram for macOS, which has none.
internal expect val platformFocusRing: Boolean

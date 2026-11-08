package com.xbot.kotogram.feature.chats.impl.extensions

import io.xbot.tdlib.api.FormattedText
import io.xbot.tdlib.api.TextEntityTypeCustomEmoji

internal fun FormattedText.customEmojiIds(): List<Long> =
    entities.mapNotNull { (it.type as? TextEntityTypeCustomEmoji)?.customEmojiId }

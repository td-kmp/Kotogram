package com.xbot.kotogram.feature.media.domain.repository

import arrow.core.Either
import com.xbot.kotogram.feature.media.domain.model.MediaError
import io.xbot.tdlib.api.Sticker

interface CustomEmojiRepository {
    suspend fun stickers(customEmojiIds: Set<Long>): Either<MediaError, Map<Long, Sticker>>
}

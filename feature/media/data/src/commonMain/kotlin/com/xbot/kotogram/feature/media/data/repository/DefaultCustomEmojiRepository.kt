package com.xbot.kotogram.feature.media.data.repository

import arrow.core.Either
import arrow.core.raise.either
import com.xbot.kotogram.core.account.extensions.sendCatching
import com.xbot.kotogram.feature.media.domain.model.MediaError
import com.xbot.kotogram.feature.media.domain.repository.CustomEmojiRepository
import io.xbot.tdlib.api.GetCustomEmojiStickers
import io.xbot.tdlib.api.Sticker
import io.xbot.tdlib.api.StickerFullTypeCustomEmoji
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class DefaultCustomEmojiRepository(
    private val client: TdClient,
) : CustomEmojiRepository {
    private val mutex = Mutex()
    private val cache = mutableMapOf<Long, Sticker>()

    override suspend fun stickers(customEmojiIds: Set<Long>): Either<MediaError, Map<Long, Sticker>> = either {
        val missing = mutex.withLock { customEmojiIds - cache.keys }
        missing.chunked(MAX_IDS_PER_REQUEST).forEach { ids ->
            val stickers = client.sendCatching(GetCustomEmojiStickers(customEmojiIds = ids))
                .mapLeft { MediaError.Unknown(it.message) }
                .bind()
            mutex.withLock {
                stickers.stickers.forEach { sticker ->
                    (sticker.fullType as? StickerFullTypeCustomEmoji)?.let {
                        cache[it.customEmojiId] =
                            sticker
                    }
                }
            }
        }
        mutex.withLock { customEmojiIds.mapNotNull { id -> cache[id]?.let { id to it } }.toMap() }
    }
}

private const val MAX_IDS_PER_REQUEST = 200

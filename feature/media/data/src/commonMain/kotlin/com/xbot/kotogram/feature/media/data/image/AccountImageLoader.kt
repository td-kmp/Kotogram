package com.xbot.kotogram.feature.media.data.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.xbot.kotogram.core.account.AccountContext
import com.xbot.kotogram.feature.media.domain.repository.FileRepository

// One memory cache for the app, its keys prefixed by account, as Telegram X's ImageCache.
internal class AccountImageLoader(
    account: AccountContext,
    platformContext: PlatformContext,
    private val memoryCache: MemoryCache,
    repository: FileRepository
) : AutoCloseable {
    private val keyPrefix = "${account.id.value}_"

    val imageLoader: ImageLoader = ImageLoader.Builder(platformContext)
        .memoryCache(memoryCache)
        .diskCache(null as DiskCache?)
        .components {
            add(TdlibFileKeyer(keyPrefix))
            add(TdlibFileFetcher.Factory(repository))
        }
        .build()

    // Not shutdown(), which clears the whole cache; Coil holds the loader's system callbacks weakly.
    override fun close() {
        memoryCache.keys.filter { it.key.startsWith(keyPrefix) }.forEach(memoryCache::remove)
    }
}

package com.xbot.kotogram.feature.media.data.di

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.memory.MemoryCache
import com.xbot.kotogram.core.account.AccountScope
import com.xbot.kotogram.feature.media.data.image.AccountImageLoader
import com.xbot.kotogram.feature.media.data.repository.DefaultCustomEmojiRepository
import com.xbot.kotogram.feature.media.data.repository.DefaultFileRepository
import com.xbot.kotogram.feature.media.domain.repository.CustomEmojiRepository
import com.xbot.kotogram.feature.media.domain.repository.FileRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.module
import org.koin.dsl.onClose

val mediaDataModule = module {
    single<PlatformContext> { platformContext() }
    single { MemoryCache.Builder().maxSizePercent(get()).build() }
    scope(AccountScope) {
        scopedOf(::DefaultFileRepository) { bind<FileRepository>() }
        scopedOf(::DefaultCustomEmojiRepository) { bind<CustomEmojiRepository>() }
        scopedOf(::AccountImageLoader) onClose { it?.close() }
        scoped<ImageLoader> { get<AccountImageLoader>().imageLoader }
    }
}

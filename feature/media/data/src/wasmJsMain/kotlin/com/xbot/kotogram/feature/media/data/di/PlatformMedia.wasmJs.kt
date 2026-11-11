package com.xbot.kotogram.feature.media.data.di

import coil3.PlatformContext
import org.koin.core.scope.Scope

internal actual val platformReadsFilePaths: Boolean = false

internal actual fun Scope.platformContext(): PlatformContext = PlatformContext.INSTANCE

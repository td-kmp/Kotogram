package com.xbot.kotogram.feature.media.data.di

import coil3.PlatformContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope

internal actual val platformReadsFilePaths: Boolean = true

internal actual fun Scope.platformContext(): PlatformContext = androidContext()

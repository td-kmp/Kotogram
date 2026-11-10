package com.xbot.kotogram.feature.media.data.di

import coil3.PlatformContext
import org.koin.core.scope.Scope

internal expect val platformReadsFilePaths: Boolean

internal expect fun Scope.platformContext(): PlatformContext

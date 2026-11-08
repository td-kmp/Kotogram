package com.xbot.kotogram.feature.media.data.di

import coil3.PlatformContext
import org.koin.core.scope.Scope

// The web's worker owns the files: there they are read through TDLib, not by path.
internal expect val platformReadsFilePaths: Boolean

internal expect fun Scope.platformContext(): PlatformContext

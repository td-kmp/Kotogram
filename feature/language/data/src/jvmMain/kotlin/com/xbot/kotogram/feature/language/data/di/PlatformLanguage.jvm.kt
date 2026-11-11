package com.xbot.kotogram.feature.language.data.di

import com.xbot.kotogram.feature.language.data.platform.PlatformLanguage
import org.koin.core.scope.Scope

internal actual fun Scope.platformLanguage(): PlatformLanguage? = null

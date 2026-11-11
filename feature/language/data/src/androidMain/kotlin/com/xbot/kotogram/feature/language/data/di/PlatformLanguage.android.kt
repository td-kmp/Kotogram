package com.xbot.kotogram.feature.language.data.di

import android.app.Application
import com.xbot.kotogram.feature.language.data.platform.AndroidPlatformLanguage
import com.xbot.kotogram.feature.language.data.platform.PlatformLanguage
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope

internal actual fun Scope.platformLanguage(): PlatformLanguage? =
    AndroidPlatformLanguage(androidContext().applicationContext as Application)

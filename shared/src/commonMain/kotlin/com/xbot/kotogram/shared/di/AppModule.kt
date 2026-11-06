package com.xbot.kotogram.shared.di

import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.polymorphic
import com.xbot.kotogram.shared.navigation.SplashRoute
import com.xbot.kotogram.shared.screen.splash.SplashScreen
import kotlinx.serialization.modules.subclass
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val appModule = module {
    polymorphic<NavKey> {
        subclass(SplashRoute::class)
    }

    navigation<SplashRoute> { SplashScreen() }
}

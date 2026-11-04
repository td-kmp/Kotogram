package com.xbot.kotogram.shared.di

import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.polymorphic
import com.xbot.kotogram.shared.root.SplashRoute
import com.xbot.kotogram.shared.root.SplashScreen
import kotlinx.serialization.modules.subclass
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

/** The app shell's own routes; features bring theirs in `<feature>FeatureModule`. */
@OptIn(KoinExperimentalAPI::class)
internal val appFeatureModule = module {
    polymorphic<NavKey> {
        subclass(SplashRoute::class)
    }

    navigation<SplashRoute> { SplashScreen() }
}

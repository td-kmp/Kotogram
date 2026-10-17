package com.xbot.kotogram.shared.di

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.polymorphic
import com.xbot.kotogram.shared.root.SplashRoute
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

    navigation<SplashRoute> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

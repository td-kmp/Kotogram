package com.xbot.kotogram.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.ProvidePlatformComponents
import com.xbot.kotogram.core.resources.LocalizationSource
import com.xbot.kotogram.core.resources.ProvideLocalization
import com.xbot.kotogram.feature.connection.impl.ConnectionEffect
import com.xbot.kotogram.shared.root.RootNavigation
import com.xbot.kotogram.shared.screen.splash.SplashScreen
import org.koin.compose.koinInject

@Composable
fun App() {
    val localization by koinInject<LocalizationSource>().localization.collectAsStateWithLifecycle()

    ConnectionEffect()
    KotogramTheme {
        ProvidePlatformComponents {
            when (val current = localization) {
                null -> SplashScreen()
                else -> ProvideLocalization(current) { RootNavigation() }
            }
        }
    }
}

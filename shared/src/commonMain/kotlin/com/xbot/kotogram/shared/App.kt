package com.xbot.kotogram.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.xbot.kotogram.core.account.Localization
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.resources.LanguagePack
import com.xbot.kotogram.core.resources.LocalLanguagePack
import com.xbot.kotogram.shared.root.RootNavigation
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.koinInject

@Composable
fun App() {
    val localization = koinInject<Localization>()
    val languagePack = remember(localization) { LocalizationLanguagePack(localization) }
    CompositionLocalProvider(LocalLanguagePack provides languagePack) {
        KotogramTheme {
            RootNavigation()
        }
    }
}

// core:account has no UI, so core:resources' interface is met here.
private class LocalizationLanguagePack(private val localization: Localization) : LanguagePack {
    override val revision: StateFlow<Int> get() = localization.revision

    override suspend fun string(key: String): String? = localization.string(key)
}

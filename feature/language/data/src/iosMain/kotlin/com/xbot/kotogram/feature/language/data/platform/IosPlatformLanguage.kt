package com.xbot.kotogram.feature.language.data.platform

import com.xbot.kotogram.feature.language.data.languagePackIdOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

internal class IosPlatformLanguage : PlatformLanguage {
    override val languagePackId: Flow<String?> = flowOf(read())

    // Workaround: iOS lets only the user choose an app's language, in Settings, so a choice made in the app stays here.
    override suspend fun set(languagePackId: String) = Unit

    private fun read(): String? {
        val bundleId = NSBundle.mainBundle.bundleIdentifier ?: return null
        val chosen = NSUserDefaults.standardUserDefaults.persistentDomainForName(bundleId)?.get(APP_LANGUAGES)
        return (chosen as? List<*>)?.filterIsInstance<String>()?.takeIf { it.isNotEmpty() }?.let(::languagePackIdOf)
    }

    private companion object {
        const val APP_LANGUAGES = "AppleLanguages"
    }
}

package com.xbot.kotogram.feature.language.data.platform

import com.xbot.kotogram.feature.language.data.languagePackIdOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

internal class IosPlatformLanguage : PlatformLanguage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override val languagePackId: Flow<String?> = flow { emit(changedLanguagePackId()) }

    override fun sync(languagePackId: String) {
        defaults.setObject(localization(), forKey = SYNCED_LOCALIZATION)
    }

    // Workaround: the app can't set its language on iOS, so only a change since the last sync is followed.
    private fun changedLanguagePackId(): String? {
        val current = localization() ?: return null
        val synced = defaults.stringForKey(SYNCED_LOCALIZATION)
        if (synced == null) defaults.setObject(current, forKey = SYNCED_LOCALIZATION)
        return current.takeIf { synced != null && it != synced }?.let(::languagePackIdOf)
    }

    private fun localization(): String? = NSBundle.mainBundle.preferredLocalizations.firstOrNull() as? String

    private companion object {
        const val SYNCED_LOCALIZATION = "syncedLocalization"
    }
}

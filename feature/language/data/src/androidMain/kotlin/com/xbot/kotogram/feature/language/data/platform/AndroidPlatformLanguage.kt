package com.xbot.kotogram.feature.language.data.platform

import android.app.LocaleConfig
import android.app.LocaleManager
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.annotation.RequiresApi
import com.xbot.kotogram.feature.language.data.extensions.toList
import com.xbot.kotogram.feature.language.data.languagePackIdOf
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class AndroidPlatformLanguage(
    private val context: Context,
) : PlatformLanguage {
    private val localeManager = context.getSystemService(LocaleManager::class.java)

    override val languagePackId: Flow<String?> = callbackFlow {
        val callbacks = object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                trySend(Unit)
            }

            @Suppress("OVERRIDE_DEPRECATION")
            override fun onLowMemory() = Unit
        }
        context.registerComponentCallbacks(callbacks)
        send(Unit)
        awaitClose { context.unregisterComponentCallbacks(callbacks) }
    }.conflate().map { read() }.distinctUntilChanged()

    override fun sync(languagePackId: String) {
        if (read() == languagePackId) return
        val locale = supportedLocales().firstOrNull { languagePackIdOf(it.toLanguageTag()) == languagePackId } ?: return
        localeManager.applicationLocales = LocaleList(locale)
    }

    private fun read(): String? {
        val supported = supportedLocales()
        return localeManager.applicationLocales.toList()
            .firstNotNullOfOrNull { chosen ->
                supported.firstOrNull { LocaleList.matchesLanguageAndScript(it, chosen) }
            }
            ?.let { languagePackIdOf(it.toLanguageTag()) }
    }

    private fun supportedLocales(): List<Locale> = LocaleConfig(context).supportedLocales?.toList().orEmpty()
}

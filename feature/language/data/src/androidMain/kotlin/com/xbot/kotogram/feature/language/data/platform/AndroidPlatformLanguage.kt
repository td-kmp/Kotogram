package com.xbot.kotogram.feature.language.data.platform

import android.app.Activity
import android.app.Application
import android.app.LocaleManager
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.xbot.kotogram.feature.language.data.languagePackIdOf
import com.xbot.kotogram.feature.language.data.languageTagOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

internal class AndroidPlatformLanguage(
    private val application: Application,
) : PlatformLanguage {
    private val current = MutableStateFlow(read())

    override val languagePackId: Flow<String?> = current.asStateFlow()

    init {
        application.registerComponentCallbacks(
            object : ComponentCallbacks {
                override fun onConfigurationChanged(newConfig: Configuration) = refresh()

                @Suppress("OVERRIDE_DEPRECATION")
                override fun onLowMemory() = Unit
            },
        )
        // Workaround: before Android 13 AppCompat reads its stored locales only when an activity attaches.
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?,
                ) = refresh()

                override fun onActivityResumed(activity: Activity) = refresh()

                override fun onActivityStarted(activity: Activity) = Unit

                override fun onActivityPaused(activity: Activity) = Unit

                override fun onActivityStopped(activity: Activity) = Unit

                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    outState: Bundle,
                ) = Unit

                override fun onActivityDestroyed(activity: Activity) = Unit
            },
        )
    }

    override suspend fun set(languagePackId: String) {
        val languageTag = languageTagOf(languagePackId) ?: return
        withContext(Dispatchers.Main) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
            refresh()
        }
    }

    private fun refresh() {
        current.value = read()
    }

    private fun read(): String? {
        val chosen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            LocaleListCompat.wrap(application.getSystemService(LocaleManager::class.java).applicationLocales)
        } else {
            AppCompatDelegate.getApplicationLocales()
        }
        if (chosen.isEmpty) return null
        return languagePackIdOf(List(chosen.size()) { chosen[it]?.toLanguageTag().orEmpty() })
    }
}

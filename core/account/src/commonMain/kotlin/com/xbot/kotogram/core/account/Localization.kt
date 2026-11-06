package com.xbot.kotogram.core.account

import co.touchlab.kermit.Logger
import io.xbot.tdlib.TdLib
import io.xbot.tdlib.api.GetLanguagePackString
import io.xbot.tdlib.api.LanguagePackStringValueOrdinary
import io.xbot.tdlib.api.TdObject
import io.xbot.tdlib.api.UpdateLanguagePackStrings
import io.xbot.tdlib.api.parseTdObject
import io.xbot.tdlib.api.toTdJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerializationException

// Strings of Telegram for macOS's language pack in the system language, as TDLib keeps them on disk.
class Localization internal constructor(
    private val tdLib: TdLib,
    storage: AccountStorage,
    config: TdlibConfig
) {
    internal val databasePath = storage.languagePackDatabase
    internal val languagePackId = config.systemLanguageCode

    private val cache = MutableStateFlow(emptyMap<String, String?>())

    private val mutableRevision = MutableStateFlow(0)
    val revision: StateFlow<Int> = mutableRevision.asStateFlow()

    // Null for a key the pack doesn't have yet: TDLib downloads the whole pack once an account sets it.
    suspend fun string(key: String): String? {
        val cached = cache.value
        if (key in cached) return cached[key]
        val value = lookUp(key)
        cache.update { it + (key to value) }
        return value
    }

    internal fun onStringsChanged(update: UpdateLanguagePackStrings) {
        val isOurs = update.localizationTarget == LOCALIZATION_TARGET &&
            update.languagePackId == languagePackId
        if (!isOurs) return
        cache.value = emptyMap()
        mutableRevision.update { it + 1 }
    }

    private suspend fun lookUp(key: String): String? {
        val request = GetLanguagePackString(
            languagePackDatabasePath = databasePath,
            localizationTarget = LOCALIZATION_TARGET,
            languagePackId = languagePackId,
            key = key
        )
        val answer = tdLib.execute(request.toTdJson())?.let(::parseOrNull)
        return (answer as? LanguagePackStringValueOrdinary)?.value
    }

    private fun parseOrNull(json: String): TdObject? = try {
        parseTdObject(json)
    } catch (e: SerializationException) {
        Logger.e(e) { "Can't read a language pack string" }
        null
    }

    internal companion object {
        const val LOCALIZATION_TARGET = "macos"
    }
}

package com.xbot.kotogram.feature.language.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import io.xbot.tdlib.api.LanguagePackInfo
import io.xbot.tdlib.api.TdJson
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException

internal interface ChosenLanguageStore {
    suspend fun load(): LanguagePackInfo?

    suspend fun save(info: LanguagePackInfo?)
}

internal class DataStoreChosenLanguageStore(private val preferences: DataStore<Preferences>) : ChosenLanguageStore {
    override suspend fun load(): LanguagePackInfo? {
        val text = preferences.data.first()[LANGUAGE] ?: return null
        return try {
            TdJson.decodeFromString(LanguagePackInfo.serializer(), text)
        } catch (e: SerializationException) {
            Logger.e(e) { "Can't read the chosen language" }
            null
        }
    }

    override suspend fun save(info: LanguagePackInfo?) {
        preferences.edit {
            if (info == null) {
                it.remove(LANGUAGE)
            } else {
                it[LANGUAGE] = TdJson.encodeToString(LanguagePackInfo.serializer(), info)
            }
        }
    }

    private companion object {
        val LANGUAGE = stringPreferencesKey("language")
    }
}

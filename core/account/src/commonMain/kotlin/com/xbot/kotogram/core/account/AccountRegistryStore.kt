package com.xbot.kotogram.core.account

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

interface AccountRegistryStore {
    suspend fun load(): AccountRegistry

    suspend fun save(registry: AccountRegistry)
}

internal class DataStoreAccountRegistryStore(
    private val preferences: DataStore<Preferences>,
) : AccountRegistryStore {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun load(): AccountRegistry {
        val text = preferences.data.first()[REGISTRY] ?: return AccountRegistry()
        return try {
            json.decodeFromString(AccountRegistry.serializer(), text)
        } catch (e: SerializationException) {
            Logger.e(e) { "Can't read the account registry" }
            AccountRegistry()
        }
    }

    override suspend fun save(registry: AccountRegistry) {
        val text = json.encodeToString(AccountRegistry.serializer(), registry)
        preferences.edit { it[REGISTRY] = text }
    }

    private companion object {
        val REGISTRY = stringPreferencesKey("registry")
    }
}

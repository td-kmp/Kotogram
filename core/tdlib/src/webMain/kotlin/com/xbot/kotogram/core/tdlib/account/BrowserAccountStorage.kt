package com.xbot.kotogram.core.tdlib.account

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.xbot.kotogram.core.model.AccountId
import kotlinx.coroutines.flow.first

/**
 * TDLib's files are the worker's, in its IndexedDB, so the page keeps the list itself. `logOut` and
 * `destroy` already erase an account's data there, so deleting one only forgets it.
 */
internal class BrowserAccountStorage(private val preferences: DataStore<Preferences>) :
    AccountStorage,
    AccountPointerStore by DataStoreAccountPointerStore(preferences) {

    override suspend fun accounts(): List<AccountId> = preferences.data.first()[ACCOUNTS]
        .orEmpty()
        .mapNotNull { it.toIntOrNull() }
        .sorted()
        .map(::AccountId)

    override suspend fun create(id: AccountId) {
        preferences.edit { it[ACCOUNTS] = it[ACCOUNTS].orEmpty() + id.value.toString() }
    }

    override suspend fun delete(id: AccountId) {
        preferences.edit { it[ACCOUNTS] = it[ACCOUNTS].orEmpty() - id.value.toString() }
    }

    private companion object {
        val ACCOUNTS = stringSetPreferencesKey("accounts")
    }
}

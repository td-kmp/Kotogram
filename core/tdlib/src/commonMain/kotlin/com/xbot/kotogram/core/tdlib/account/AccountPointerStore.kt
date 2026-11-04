package com.xbot.kotogram.core.tdlib.account

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.xbot.kotogram.core.model.AccountId
import kotlinx.coroutines.flow.first

/** What the app itself remembers about accounts. Everything else is TDLib's. */
internal data class AccountPointers(
    val active: AccountId? = null,
    val previous: AccountId? = null,
    val lastId: Int = 0
)

internal interface AccountPointerStore {
    suspend fun pointers(): AccountPointers

    suspend fun savePointers(pointers: AccountPointers)
}

internal class DataStoreAccountPointerStore(private val preferences: DataStore<Preferences>) :
    AccountPointerStore {

    override suspend fun pointers(): AccountPointers {
        val saved = preferences.data.first()
        return AccountPointers(
            active = saved[ACTIVE]?.let(::AccountId),
            previous = saved[PREVIOUS]?.let(::AccountId),
            lastId = saved[LAST_ID] ?: 0
        )
    }

    override suspend fun savePointers(pointers: AccountPointers) {
        preferences.edit { saved ->
            pointers.active?.let { saved[ACTIVE] = it.value }
            pointers.previous?.let { saved[PREVIOUS] = it.value }
            saved[LAST_ID] = pointers.lastId
        }
    }

    private companion object {
        val ACTIVE = intPreferencesKey("active_account")
        val PREVIOUS = intPreferencesKey("previous_account")
        val LAST_ID = intPreferencesKey("last_account_id")
    }
}

package com.xbot.kotogram.core.data.account

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.xbot.kotogram.core.data.environment.ACCOUNT_PREFIX
import com.xbot.kotogram.core.data.environment.TdlibEnvironment
import com.xbot.kotogram.core.data.environment.databaseDirectoryOf
import com.xbot.kotogram.core.data.environment.filesDirectoryOf
import com.xbot.kotogram.core.domain.model.AccountId
import kotlinx.coroutines.flow.first
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import okio.Path.Companion.toPath

internal actual fun platformAccountStorage(environment: TdlibEnvironment): AccountStorage =
    DirectoryAccountStorage(environment)

/** The directories under the database directory are the accounts. */
private class DirectoryAccountStorage(private val environment: TdlibEnvironment) : AccountStorage {
    private val preferences = PreferenceDataStoreFactory.createWithPath {
        "${environment.databaseDirectory}/accounts.preferences_pb".toPath()
    }

    override suspend fun accounts(): List<AccountId> {
        SystemFileSystem.createDirectories(Path(environment.databaseDirectory))
        return SystemFileSystem.list(Path(environment.databaseDirectory))
            .map { it.name }
            .filter { it.startsWith(ACCOUNT_PREFIX) }
            .mapNotNull { it.removePrefix(ACCOUNT_PREFIX).toIntOrNull() }
            .sorted()
            .map(::AccountId)
    }

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

    override fun create(id: AccountId) {
        SystemFileSystem.createDirectories(Path(environment.databaseDirectoryOf(id)))
    }

    override fun delete(id: AccountId) {
        deleteRecursively(Path(environment.databaseDirectoryOf(id)))
        deleteRecursively(Path(environment.filesDirectoryOf(id)))
    }

    private fun deleteRecursively(path: Path) {
        if (!SystemFileSystem.exists(path)) return
        if (SystemFileSystem.metadataOrNull(path)?.isDirectory == true) {
            SystemFileSystem.list(path).forEach(::deleteRecursively)
        }
        SystemFileSystem.delete(path, mustExist = false)
    }

    private companion object {
        val ACTIVE = intPreferencesKey("active_account")
        val PREVIOUS = intPreferencesKey("previous_account")
        val LAST_ID = intPreferencesKey("last_account_id")
    }
}

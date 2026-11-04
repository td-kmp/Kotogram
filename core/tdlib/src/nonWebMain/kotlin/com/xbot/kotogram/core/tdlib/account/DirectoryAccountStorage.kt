package com.xbot.kotogram.core.tdlib.account

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.core.tdlib.environment.TdlibEnvironment
import com.xbot.kotogram.core.tdlib.extensions.ACCOUNT_PREFIX
import com.xbot.kotogram.core.tdlib.extensions.databaseDirectoryOf
import com.xbot.kotogram.core.tdlib.extensions.filesDirectoryOf
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/** The directories under the database directory are the accounts. */
internal class DirectoryAccountStorage(
    private val environment: TdlibEnvironment,
    preferences: DataStore<Preferences>
) : AccountStorage,
    AccountPointerStore by DataStoreAccountPointerStore(preferences) {

    override suspend fun accounts(): List<AccountId> {
        SystemFileSystem.createDirectories(Path(environment.databaseDirectory))
        return SystemFileSystem.list(Path(environment.databaseDirectory))
            .map { it.name }
            .filter { it.startsWith(ACCOUNT_PREFIX) }
            .mapNotNull { it.removePrefix(ACCOUNT_PREFIX).toIntOrNull() }
            .sorted()
            .map(::AccountId)
    }

    override suspend fun create(id: AccountId) {
        SystemFileSystem.createDirectories(Path(environment.databaseDirectoryOf(id)))
    }

    override suspend fun delete(id: AccountId) {
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
}

package com.xbot.kotogram.core.account

// The worker keeps only /tdlib in IndexedDB; anything outside it is gone with the worker.
internal class BrowserAccountStorage(private val root: String) : AccountStorage {
    override fun databaseDirectory(id: AccountId): String = "$root/${id.value}/db"

    override fun filesDirectory(id: AccountId): String = "$root/${id.value}/files"

    override val languagePackDatabase: String = "$root/langpack"

    // The files are the worker's, and TDLib has already erased them on a log out.
    override suspend fun delete(id: AccountId) = Unit
}

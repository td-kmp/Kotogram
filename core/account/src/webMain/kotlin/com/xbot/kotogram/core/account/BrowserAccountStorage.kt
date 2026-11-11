package com.xbot.kotogram.core.account

internal class BrowserAccountStorage(
    private val root: String,
) : AccountStorage {
    override fun databaseDirectory(id: AccountId): String = "$root/${id.value}/db"

    override fun filesDirectory(id: AccountId): String = "$root/${id.value}/files"

    override suspend fun delete(id: AccountId) = Unit
}

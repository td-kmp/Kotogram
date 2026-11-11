package com.xbot.kotogram.core.account

interface AccountStorage {
    fun databaseDirectory(id: AccountId): String

    fun filesDirectory(id: AccountId): String

    suspend fun delete(id: AccountId)
}

internal val accountsDirectoryName: String get() =
    if (TelegramApiConfig.USE_TEST_DC) "accounts-test" else "accounts"

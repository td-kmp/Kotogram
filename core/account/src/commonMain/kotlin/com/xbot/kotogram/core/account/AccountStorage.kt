package com.xbot.kotogram.core.account

interface AccountStorage {
    fun databaseDirectory(id: AccountId): String

    fun filesDirectory(id: AccountId): String

    val languagePackDatabase: String

    suspend fun delete(id: AccountId)
}

// A database created on the production servers can't be used on the test ones, so they live apart.
internal val accountsDirectoryName: String get() =
    if (TelegramApiConfig.USE_TEST_DC) "accounts-test" else "accounts"

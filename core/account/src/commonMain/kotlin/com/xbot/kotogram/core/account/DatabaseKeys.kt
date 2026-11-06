package com.xbot.kotogram.core.account

interface DatabaseKeys {
    suspend fun getOrCreate(id: AccountId): ByteArray?

    suspend fun delete(id: AccountId)
}

// TDLib treats an empty key as no encryption.
internal object NoDatabaseKeys : DatabaseKeys {
    override suspend fun getOrCreate(id: AccountId): ByteArray = ByteArray(0)

    override suspend fun delete(id: AccountId) = Unit
}

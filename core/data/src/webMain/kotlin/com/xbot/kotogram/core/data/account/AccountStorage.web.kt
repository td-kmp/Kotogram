package com.xbot.kotogram.core.data.account

import com.xbot.kotogram.core.data.environment.TdlibEnvironment
import com.xbot.kotogram.core.domain.model.AccountId
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

internal actual fun platformAccountStorage(environment: TdlibEnvironment): AccountStorage =
    BrowserAccountStorage(prefix = "kotogram${environment.databaseDirectory}")

/**
 * TDLib's files are the worker's, in its IndexedDB, so the page keeps the list itself. `logOut` and
 * `destroy` already erase an account's data there, so deleting one only forgets it.
 */
private class BrowserAccountStorage(private val prefix: String) : AccountStorage {
    override suspend fun accounts(): List<AccountId> = read(ACCOUNTS)
        ?.split(',')
        ?.mapNotNull { it.toIntOrNull() }
        ?.sorted()
        ?.map(::AccountId)
        .orEmpty()

    override suspend fun pointers() = AccountPointers(
        active = read(ACTIVE)?.toIntOrNull()?.let(::AccountId),
        previous = read(PREVIOUS)?.toIntOrNull()?.let(::AccountId),
        lastId = read(LAST_ID)?.toIntOrNull() ?: 0
    )

    override suspend fun savePointers(pointers: AccountPointers) {
        pointers.active?.let { write(ACTIVE, it.value.toString()) }
        pointers.previous?.let { write(PREVIOUS, it.value.toString()) }
        write(LAST_ID, pointers.lastId.toString())
    }

    override fun create(id: AccountId) = saveAccounts(storedIds() + id.value)

    override fun delete(id: AccountId) = saveAccounts(storedIds() - id.value)

    private fun storedIds() = read(ACCOUNTS)?.split(',')?.mapNotNull { it.toIntOrNull() }.orEmpty()

    private fun saveAccounts(ids: List<Int>) = write(ACCOUNTS, ids.joinToString(","))

    private fun read(key: String): String? = localStorage["$prefix/$key"]

    private fun write(key: String, value: String) {
        localStorage["$prefix/$key"] = value
    }

    private companion object {
        const val ACCOUNTS = "accounts"
        const val ACTIVE = "active"
        const val PREVIOUS = "previous"
        const val LAST_ID = "lastId"
    }
}

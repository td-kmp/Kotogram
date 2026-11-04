package com.xbot.kotogram.core.data.account

import com.xbot.kotogram.core.data.environment.TdlibEnvironment
import com.xbot.kotogram.core.domain.model.AccountId
import org.koin.core.annotation.Singleton

/** What the app itself remembers about accounts. Everything else is TDLib's. */
internal data class AccountPointers(
    val active: AccountId? = null,
    val previous: AccountId? = null,
    val lastId: Int = 0
)

/**
 * Which accounts exist. On disk that is the directories next to TDLib's files; on the web those belong
 * to the worker, so the page keeps its own list.
 */
internal interface AccountStorage {
    suspend fun accounts(): List<AccountId>

    suspend fun pointers(): AccountPointers

    suspend fun savePointers(pointers: AccountPointers)

    fun create(id: AccountId)

    fun delete(id: AccountId)
}

// A class, not a provider function: see PlatformTdlibEnvironment.
@Singleton
internal class PlatformAccountStorage(environment: TdlibEnvironment) :
    AccountStorage by platformAccountStorage(environment)

internal expect fun platformAccountStorage(environment: TdlibEnvironment): AccountStorage

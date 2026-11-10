package com.xbot.kotogram.core.account

import kotlinx.serialization.Serializable

@Serializable
data class AccountEntry(
    val id: AccountId,
    val userId: Long? = null,
)

@Serializable
data class AccountRegistry(
    val accounts: List<AccountEntry> = emptyList(),
    val active: AccountId? = null,
    val lastId: Int = 0,
) {
    val signedIn: List<AccountEntry> get() = accounts.filter { it.userId != null }

    val newestId: AccountId get() = AccountId(lastId)

    fun entry(id: AccountId): AccountEntry? = accounts.firstOrNull { it.id == id }

    fun withNewAccount(): AccountRegistry = copy(
        accounts = accounts + AccountEntry(AccountId(lastId + 1)),
        lastId = lastId + 1,
    )

    fun update(
        id: AccountId,
        change: (AccountEntry) -> AccountEntry,
    ): AccountRegistry = copy(accounts = accounts.map { if (it.id == id) change(it) else it })

    fun without(id: AccountId): AccountRegistry {
        val rest = accounts.filterNot { it.id == id }
        val nextActive = if (active == id) rest.firstOrNull { it.userId != null }?.id else active
        return copy(accounts = rest, active = nextActive)
    }
}

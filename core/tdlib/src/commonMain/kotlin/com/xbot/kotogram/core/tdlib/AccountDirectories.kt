package com.xbot.kotogram.core.tdlib

import com.xbot.kotogram.core.domain.model.Account
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.core.tdlib.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.core.Koin
import org.koin.core.scope.Scope

// An account is the TDLib database in its directory; its Koin scope has the same name.
internal const val ACCOUNT_PREFIX = "account-"

internal val AccountId.directoryName: String get() = "$ACCOUNT_PREFIX$value"

internal fun TdlibEnvironment.databaseDirectoryOf(id: AccountId) =
    "$databaseDirectory/${id.directoryName}"

internal fun TdlibEnvironment.filesDirectoryOf(id: AccountId) =
    "$filesDirectory/${id.directoryName}"

/** The Koin scope of an open account, for the UI to provide to the account's screens. */
fun Koin.accountScope(id: AccountId): Scope? = getScopeOrNull(id.directoryName)

internal fun Koin.holderOf(id: AccountId): ClientHolder =
    checkNotNull(accountScope(id)) { "Account ${id.value} is not open" }.get()

internal fun Koin.accountOf(id: AccountId): Flow<Account> {
    val accountScope = checkNotNull(accountScope(id)) { "Account ${id.value} is not open" }
    val userId = accountScope.get<AccountStore>().state.map { it.myId }.distinctUntilChanged()
    return combine(accountScope.get<ClientHolder>().authorizationState, userId) { state, myId ->
        Account(id = id, authorizationState = state?.toDomain(), userId = myId)
    }
}

internal fun TdlibEnvironment.deleteDirectoriesOf(id: AccountId) {
    deleteRecursively(Path(databaseDirectoryOf(id)))
    deleteRecursively(Path(filesDirectoryOf(id)))
}

private fun deleteRecursively(path: Path) {
    if (!SystemFileSystem.exists(path)) return
    if (SystemFileSystem.metadataOrNull(path)?.isDirectory == true) {
        SystemFileSystem.list(path).forEach(::deleteRecursively)
    }
    SystemFileSystem.delete(path, mustExist = false)
}

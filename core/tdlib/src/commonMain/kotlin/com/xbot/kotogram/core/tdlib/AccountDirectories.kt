package com.xbot.kotogram.core.tdlib

import com.xbot.kotogram.core.domain.model.AccountId
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

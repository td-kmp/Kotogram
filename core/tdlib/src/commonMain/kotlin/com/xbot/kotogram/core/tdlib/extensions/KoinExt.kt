package com.xbot.kotogram.core.tdlib.extensions

import com.xbot.kotogram.core.model.AccountId
import org.koin.core.Koin
import org.koin.core.scope.Scope

/** The Koin scope of an open account, for the UI to provide to the account's screens. */
fun Koin.accountScope(id: AccountId): Scope? = getScopeOrNull(id.directoryName)

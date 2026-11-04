package com.xbot.kotogram.core.tdlib.extensions

import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.core.tdlib.account.AccountSession
import com.xbot.kotogram.core.tdlib.account.AccountSessions

internal fun AccountSessions.getValue(id: AccountId): AccountSession =
    checkNotNull(get(id)) { "Account ${id.value} is not open" }

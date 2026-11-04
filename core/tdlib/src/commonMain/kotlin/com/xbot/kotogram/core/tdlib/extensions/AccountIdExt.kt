package com.xbot.kotogram.core.tdlib.extensions

import com.xbot.kotogram.core.model.AccountId

// An account is the TDLib database in its directory; its Koin scope has the same name.
internal const val ACCOUNT_PREFIX = "account-"

internal val AccountId.directoryName: String get() = "$ACCOUNT_PREFIX$value"

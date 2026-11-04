package com.xbot.kotogram.core.tdlib.extensions

import com.xbot.kotogram.core.tdlib.store.reduce.AccountReducer
import com.xbot.kotogram.core.tdlib.store.AccountState
import io.xbot.tdlib.api.Update

internal fun AccountState.reduce(update: Update): AccountState =
    AccountReducer.reduce(this, update)

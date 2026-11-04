package com.xbot.kotogram.core.tdlib.store.reduce

import com.xbot.kotogram.core.tdlib.store.AccountState
import io.xbot.tdlib.api.Update

/** Pure, and returns [state] itself when [update] changes nothing. */
internal fun interface Reducer {
    fun reduce(state: AccountState, update: Update): AccountState
}

/** Applies [reducers] in this order, so the order is code and not whatever a DI container returns. */
internal class CompositeReducer(private vararg val reducers: Reducer) : Reducer {
    override fun reduce(state: AccountState, update: Update): AccountState =
        reducers.fold(state) { reduced, reducer -> reducer.reduce(reduced, update) }
}

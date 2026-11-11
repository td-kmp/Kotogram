package com.xbot.kotogram.feature.chats.data.store.reduce

import com.xbot.kotogram.feature.chats.data.store.ChatsState
import io.xbot.tdlib.api.Update

internal fun interface Reducer {
    fun reduce(
        state: ChatsState,
        update: Update,
    ): ChatsState
}

// Workaround: Koin's getAll gives no order, so the reducers are applied in this one.
internal class CompositeReducer(
    private vararg val reducers: Reducer,
) : Reducer {
    override fun reduce(
        state: ChatsState,
        update: Update,
    ): ChatsState = reducers.fold(state) { reduced, reducer -> reducer.reduce(reduced, update) }
}

package com.xbot.kotogram.feature.chats.data.store.reduce

import com.xbot.kotogram.feature.chats.data.store.ChatsState
import io.xbot.tdlib.api.Update

// Returns state itself when the update changes nothing: identity checks downstream rely on it.
internal fun interface Reducer {
    fun reduce(state: ChatsState, update: Update): ChatsState
}

// Applies the reducers in this order: getAll from DI gives none.
internal class CompositeReducer(private vararg val reducers: Reducer) : Reducer {
    override fun reduce(state: ChatsState, update: Update): ChatsState =
        reducers.fold(state) { reduced, reducer -> reducer.reduce(reduced, update) }
}

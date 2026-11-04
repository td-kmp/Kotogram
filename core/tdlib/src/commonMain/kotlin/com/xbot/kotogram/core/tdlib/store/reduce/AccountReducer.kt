package com.xbot.kotogram.core.tdlib.store.reduce

/** A new entity is one more reducer here. UpdateCoverageTest fails when TDLib adds an update type. */
internal object AccountReducer : Reducer by CompositeReducer(
    SessionReducer,
    UserReducer,
    GroupReducer,
    // Before ChatReducer: it compares the update's positions with the chat's current ones.
    ChatListReducer,
    ChatReducer
)

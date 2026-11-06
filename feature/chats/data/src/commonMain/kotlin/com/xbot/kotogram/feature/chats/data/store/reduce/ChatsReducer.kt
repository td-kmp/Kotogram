package com.xbot.kotogram.feature.chats.data.store.reduce

internal object ChatsReducer : Reducer by CompositeReducer(
    UserReducer,
    // Before ChatReducer: it compares the update's positions with the chat's current ones.
    ChatListReducer,
    ChatReducer
)

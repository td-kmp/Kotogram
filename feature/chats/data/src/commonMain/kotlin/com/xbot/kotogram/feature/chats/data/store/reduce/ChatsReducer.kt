package com.xbot.kotogram.feature.chats.data.store.reduce

internal object ChatsReducer : Reducer by CompositeReducer(
    UserReducer,
    FolderReducer,
    ChatListReducer,
    ChatReducer,
)

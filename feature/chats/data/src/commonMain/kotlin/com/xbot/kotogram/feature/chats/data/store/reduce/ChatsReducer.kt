package com.xbot.kotogram.feature.chats.data.store.reduce

internal object ChatsReducer : Reducer by CompositeReducer(
    UserReducer,
    PeerReducer,
    FolderReducer,
    ChatListReducer,
    ChatReducer,
    ActivityReducer,
    ForumReducer,
)

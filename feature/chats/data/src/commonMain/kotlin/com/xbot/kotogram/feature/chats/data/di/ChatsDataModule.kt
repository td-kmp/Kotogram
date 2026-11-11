package com.xbot.kotogram.feature.chats.data.di

import com.xbot.kotogram.core.account.AccountScope
import com.xbot.kotogram.core.account.AccountUpdates
import com.xbot.kotogram.feature.chats.data.repository.DefaultChatRepository
import com.xbot.kotogram.feature.chats.data.store.ChatStore
import com.xbot.kotogram.feature.chats.domain.repository.ChatRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.module

val chatsDataModule = module {
    scope(AccountScope) {
        scopedOf(::ChatStore) { bind<AccountUpdates>() }
        scopedOf(::DefaultChatRepository) { bind<ChatRepository>() }
    }
}

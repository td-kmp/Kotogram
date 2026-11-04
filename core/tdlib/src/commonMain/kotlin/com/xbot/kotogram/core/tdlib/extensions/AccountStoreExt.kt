package com.xbot.kotogram.core.tdlib.extensions

import com.xbot.kotogram.core.tdlib.store.AccountState
import com.xbot.kotogram.core.tdlib.store.AccountStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * One entity of the snapshot, compared by identity: `equals` on DTOs is deep and has a ByteArray hole.
 * Every publish resumes every selector, so a screen has one selector, not one per row.
 */
fun <T> AccountStore.select(selector: (AccountState) -> T): Flow<T> = state
    .map(selector)
    .distinctUntilChanged { old, new -> old === new }

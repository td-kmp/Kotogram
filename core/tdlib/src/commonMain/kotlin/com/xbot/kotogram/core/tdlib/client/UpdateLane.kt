package com.xbot.kotogram.core.tdlib.client

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.annotation.Singleton

/** One writer at a time for all accounts' stores, like Telegram X's one TDLib thread. */
@Singleton
internal class UpdateLane {
    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)
}

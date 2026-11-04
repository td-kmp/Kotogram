package com.xbot.kotogram.core.tdlib.client

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
internal actual fun runAtExit(block: suspend () -> Unit) {
    GlobalScope.launch { block() }
}

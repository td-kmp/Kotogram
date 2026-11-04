package com.xbot.kotogram.core.tdlib.client

import kotlinx.coroutines.runBlocking

internal actual fun runAtExit(block: suspend () -> Unit) = runBlocking { block() }

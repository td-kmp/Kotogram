package com.xbot.kotogram.core.tdlib.client

/** For closing TDLib from a scope callback: waits where the platform can, the web can't block. */
internal expect fun runAtExit(block: suspend () -> Unit)

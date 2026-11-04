package com.xbot.kotogram.core.data.client

import io.xbot.tdlib.NativeTdLib
import io.xbot.tdlib.TdLib
import kotlinx.coroutines.runBlocking

internal actual fun platformTdLib(): TdLib = NativeTdLib

internal actual fun runAtExit(block: suspend () -> Unit) = runBlocking { block() }

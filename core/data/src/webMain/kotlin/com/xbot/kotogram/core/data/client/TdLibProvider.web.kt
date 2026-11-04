package com.xbot.kotogram.core.data.client

import io.xbot.tdlib.TdLib
import io.xbot.tdlib.WorkerTdLib
import kotlin.js.ExperimentalWasmJsInterop
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Worker

internal actual fun platformTdLib(): TdLib = WorkerTdLib(createWorker())

@OptIn(ExperimentalWasmJsInterop::class)
private fun createWorker(): Worker =
    js("""new Worker(new URL("tdlib-worker/worker.js", import.meta.url), { type: "module" })""")

@OptIn(DelicateCoroutinesApi::class)
internal actual fun runAtExit(block: suspend () -> Unit) {
    GlobalScope.launch { block() }
}

package com.xbot.kotogram.core.tdlib.di

import io.xbot.tdlib.TdLib
import io.xbot.tdlib.WorkerTdLib
import kotlin.js.ExperimentalWasmJsInterop
import org.w3c.dom.Worker

internal actual fun platformTdLib(): TdLib = WorkerTdLib(createWorker())

@OptIn(ExperimentalWasmJsInterop::class)
private fun createWorker(): Worker =
    js("""new Worker(new URL("tdlib-worker/worker.js", import.meta.url), { type: "module" })""")

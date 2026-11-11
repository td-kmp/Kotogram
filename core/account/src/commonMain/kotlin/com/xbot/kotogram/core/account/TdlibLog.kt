package com.xbot.kotogram.core.account

import co.touchlab.kermit.Logger
import io.xbot.tdlib.TdLib
import io.xbot.tdlib.api.LogStreamEmpty
import io.xbot.tdlib.api.SetLogStream
import io.xbot.tdlib.api.SetLogVerbosityLevel
import io.xbot.tdlib.api.toTdJson
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
internal object TdlibLog {
    private val installed = AtomicBoolean(false)
    private val logger = Logger.withTag("TDLib")

    suspend fun install(tdLib: TdLib) {
        if (installed.exchange(true)) return
        val verbosity = TelegramApiConfig.TDLIB_LOG_VERBOSITY
        tdLib.execute(SetLogStream(logStream = LogStreamEmpty).toTdJson())
        tdLib.execute(SetLogVerbosityLevel(newVerbosityLevel = verbosity).toTdJson())
        // Workaround: TDLib's threads must not call it back, and it aborts right after a level 0 message.
        tdLib.setLogMessageHandler(verbosity) { level, message ->
            when (level) {
                FATAL -> logger.a(message)
                ERROR -> logger.e(message)
                WARNING -> logger.w(message)
                INFO -> logger.i(message)
                DEBUG -> logger.d(message)
                else -> logger.v(message)
            }
        }
    }

    private const val FATAL = 0
    private const val ERROR = 1
    private const val WARNING = 2
    private const val INFO = 3
    private const val DEBUG = 4
}

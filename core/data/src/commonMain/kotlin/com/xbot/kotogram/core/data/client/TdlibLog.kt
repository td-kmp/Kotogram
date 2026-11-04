package com.xbot.kotogram.core.data.client

import co.touchlab.kermit.Logger
import com.xbot.kotogram.core.data.TelegramApiConfig
import io.xbot.tdlib.TdLib
import io.xbot.tdlib.api.LogStreamEmpty
import io.xbot.tdlib.api.SetLogStream
import io.xbot.tdlib.api.SetLogVerbosityLevel
import io.xbot.tdlib.api.toTdJson
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Sends TDLib's process-wide log to Kermit instead of stderr, at the verbosity set by the
 * `tdlib.log.verbosity` build property. Runs once, before the first TDLib instance.
 */
@OptIn(ExperimentalAtomicApi::class)
internal object TdlibLog {
    private val installed = AtomicBoolean(false)
    private val logger = Logger.withTag("TDLib")

    suspend fun install(tdLib: TdLib) {
        if (installed.exchange(true)) return
        val verbosity = TelegramApiConfig.TDLIB_LOG_VERBOSITY
        tdLib.execute(SetLogStream(logStream = LogStreamEmpty).toTdJson())
        tdLib.execute(SetLogVerbosityLevel(newVerbosityLevel = verbosity).toTdJson())
        // Called on TDLib's threads, which must not call TDLib back; a level 0 message is fatal and
        // TDLib aborts right after it returns.
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

    // TDLib's verbosity levels (setLogVerbosityLevel); 5 and above are verbose.
    private const val FATAL = 0
    private const val ERROR = 1
    private const val WARNING = 2
    private const val INFO = 3
    private const val DEBUG = 4
}

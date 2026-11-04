package com.xbot.kotogram.core.tdlib.account

/** One account's end, for whatever must go with it. The same contract as `ViewModel.addCloseable`. */
interface AccountLifetime {
    fun addCloseable(closeable: AutoCloseable)
}

package com.xbot.kotogram.feature.connection.data.repository

import co.touchlab.kermit.Logger
import com.xbot.kotogram.feature.connection.data.platform.NetworkMonitor
import com.xbot.kotogram.feature.connection.domain.repository.ConnectionRepository
import io.xbot.tdlib.api.NetworkType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.shareIn

internal class DefaultConnectionRepository(
    appScope: CoroutineScope,
    network: NetworkMonitor?,
) : ConnectionRepository {
    val inForeground: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val networkTypes: SharedFlow<NetworkType> = (network?.changes ?: emptyFlow())
        .catch { Logger.withTag("ConnectionRepository").w(it) { "The network monitor stopped" } }
        .shareIn(appScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1)

    override fun setInForeground(inForeground: Boolean) {
        this.inForeground.value = inForeground
    }
}

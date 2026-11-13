package com.xbot.kotogram.feature.connection.data.repository

import com.xbot.kotogram.feature.connection.data.extensions.delayedAfterReady
import com.xbot.kotogram.feature.connection.data.store.ConnectionStore
import com.xbot.kotogram.feature.connection.domain.repository.ConnectionStateRepository
import io.xbot.tdlib.api.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

internal class DefaultConnectionStateRepository(
    store: ConnectionStore,
) : ConnectionStateRepository {
    override val state: Flow<ConnectionState> = store.state.filterNotNull().delayedAfterReady().distinctUntilChanged()
}

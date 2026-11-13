package com.xbot.kotogram.feature.connection.domain.repository

import io.xbot.tdlib.api.ConnectionState
import kotlinx.coroutines.flow.Flow

interface ConnectionStateRepository {
    val state: Flow<ConnectionState>
}

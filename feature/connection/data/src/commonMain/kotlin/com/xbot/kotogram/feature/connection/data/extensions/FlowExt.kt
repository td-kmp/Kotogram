package com.xbot.kotogram.feature.connection.data.extensions

import io.xbot.tdlib.api.ConnectionState
import io.xbot.tdlib.api.ConnectionStateReady
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformLatest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
internal fun Flow<ConnectionState>.delayedAfterReady(delay: Duration = AFTER_READY_DELAY): Flow<ConnectionState> =
    flow {
        var previous: ConnectionState? = null
        val delayed = transformLatest { state ->
            val after = previous
            previous = state
            if (after == ConnectionStateReady && state != ConnectionStateReady) delay(delay)
            emit(state)
        }
        emitAll(delayed)
    }

private val AFTER_READY_DELAY = 300.milliseconds

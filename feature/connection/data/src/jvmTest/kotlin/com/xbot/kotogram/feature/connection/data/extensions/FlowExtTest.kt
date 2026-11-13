package com.xbot.kotogram.feature.connection.data.extensions

import io.xbot.tdlib.api.ConnectionState
import io.xbot.tdlib.api.ConnectionStateConnecting
import io.xbot.tdlib.api.ConnectionStateReady
import io.xbot.tdlib.api.ConnectionStateUpdating
import io.xbot.tdlib.api.ConnectionStateWaitingForNetwork
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FlowExtTest {
    @Test
    fun aStateAfterReadyShowsLate() = runTest {
        val shown = collect(0L to ConnectionStateReady, 1_000L to ConnectionStateConnecting)
        assertEquals([0L to ConnectionStateReady, 1_300L to ConnectionStateConnecting], shown)
    }

    @Test
    fun aStateGoneInTimeNeverShows() = runTest {
        val shown = collect(
            0L to ConnectionStateReady,
            1_000L to ConnectionStateConnecting,
            1_200L to ConnectionStateReady,
        )
        assertEquals([0L to ConnectionStateReady, 1_200L to ConnectionStateReady], shown)
    }

    @Test
    fun aStateAfterAnotherShowsAtOnce() = runTest {
        val shown = collect(
            0L to ConnectionStateWaitingForNetwork,
            500L to ConnectionStateConnecting,
            900L to ConnectionStateUpdating,
        )
        val expected = [
            0L to ConnectionStateWaitingForNetwork,
            500L to ConnectionStateConnecting,
            900L to ConnectionStateUpdating,
        ]
        assertEquals(expected, shown)
    }

    @Test
    fun theDelayCountsFromTheStateAfterReady() = runTest {
        val shown = collect(
            0L to ConnectionStateReady,
            1_000L to ConnectionStateConnecting,
            1_100L to ConnectionStateUpdating,
        )
        assertEquals([0L to ConnectionStateReady, 1_100L to ConnectionStateUpdating], shown)
    }

    private suspend fun TestScope.collect(
        vararg states: Pair<Long, ConnectionState>,
    ): List<Pair<Long, ConnectionState>> {
        val source = flow {
            for ((at, state) in states) {
                delay(at - currentTime)
                emit(state)
            }
        }
        val shown = mutableListOf<Pair<Long, ConnectionState>>()
        val job = launch { source.delayedAfterReady().collect { shown += currentTime to it } }
        advanceUntilIdle()
        job.join()
        return shown
    }
}

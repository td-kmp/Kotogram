package com.xbot.kotogram.core.designsystem.appkit

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class ActionFlow<T>(
    value: () -> T,
) {
    private val events = MutableSharedFlow<T>(extraBufferCapacity = EVENT_BUFFER)

    val target = ActionTarget { events.tryEmit(value()) }

    val flow: Flow<T> get() = events
}

// Workaround: the bridge's flows carry no Unit, so a click is its number.
internal fun clickFlow(): ActionFlow<Int> {
    var count = 0
    return ActionFlow { ++count }
}

private const val EVENT_BUFFER = 16

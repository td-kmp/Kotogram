package com.xbot.kotogram.core.tdlib.extensions

import io.xbot.tdlib.api.Error
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val NOT_FOUND = 404
private const val TOO_MANY_REQUESTS = 429
private val retryAfterPattern = Regex("""retry after (\d+)""")

val Error.isNotFound: Boolean get() = code == NOT_FOUND

val Error.isFlood: Boolean
    get() = code == TOO_MANY_REQUESTS ||
        message.startsWith("FLOOD_WAIT") ||
        message == "PHONE_NUMBER_FLOOD"

// TDLib reports "Too Many Requests: retry after N"; the server's raw form is FLOOD_WAIT_N.
val Error.retryAfter: Duration?
    get() = (
        retryAfterPattern.find(message)?.groupValues?.get(1)?.toIntOrNull()
            ?: message.substringAfterLast('_').toIntOrNull()
        )?.seconds

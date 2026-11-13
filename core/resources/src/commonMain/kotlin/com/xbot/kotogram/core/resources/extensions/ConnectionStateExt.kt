package com.xbot.kotogram.core.resources.extensions

import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.StringResource
import com.xbot.kotogram.core.resources.connectionstatus_connecting
import com.xbot.kotogram.core.resources.connectionstatus_connectingtoproxy
import com.xbot.kotogram.core.resources.connectionstatus_updating
import com.xbot.kotogram.core.resources.connectionstatus_waitingfornetwork
import io.xbot.tdlib.api.ConnectionState
import io.xbot.tdlib.api.ConnectionStateConnecting
import io.xbot.tdlib.api.ConnectionStateConnectingToProxy
import io.xbot.tdlib.api.ConnectionStateReady
import io.xbot.tdlib.api.ConnectionStateUpdating
import io.xbot.tdlib.api.ConnectionStateWaitingForNetwork

val ConnectionState.title: StringResource?
    get() = when (this) {
        ConnectionStateWaitingForNetwork -> StringResource.Text(Res.string.connectionstatus_waitingfornetwork)
        ConnectionStateConnectingToProxy -> StringResource.Text(Res.string.connectionstatus_connectingtoproxy)
        ConnectionStateConnecting -> StringResource.Text(Res.string.connectionstatus_connecting)
        ConnectionStateUpdating -> StringResource.Text(Res.string.connectionstatus_updating)
        ConnectionStateReady -> null
    }

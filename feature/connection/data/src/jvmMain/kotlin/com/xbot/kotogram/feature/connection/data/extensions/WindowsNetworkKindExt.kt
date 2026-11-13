package com.xbot.kotogram.feature.connection.data.extensions

import com.xbot.kotogram.core.platform.windows.WindowsNetworkKind
import io.xbot.tdlib.api.NetworkType
import io.xbot.tdlib.api.NetworkTypeNone
import io.xbot.tdlib.api.NetworkTypeOther

internal fun WindowsNetworkKind.toNetworkType(): NetworkType = when (this) {
    None -> NetworkTypeNone
    Other -> NetworkTypeOther
}

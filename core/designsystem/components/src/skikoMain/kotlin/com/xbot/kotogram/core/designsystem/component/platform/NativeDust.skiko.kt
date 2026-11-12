package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.ui.node.CompositionLocalConsumerModifierNode

internal actual fun CompositionLocalConsumerModifierNode.nativeDust(
    onFrame: (frameTimeNanos: Long) -> Unit,
): NativeDust? = null

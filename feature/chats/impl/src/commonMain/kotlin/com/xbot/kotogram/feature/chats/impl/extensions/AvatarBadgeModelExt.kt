package com.xbot.kotogram.feature.chats.impl.extensions

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.designsystem.component.AvatarBadge
import com.xbot.kotogram.feature.chats.impl.screen.list.AvatarBadgeModel

@Composable
internal fun AvatarBadgeModel.toBadge(): AvatarBadge = when (this) {
    Online -> AvatarBadge.Online
    VoiceChat -> AvatarBadge.VoiceChat
    is AutoDelete -> AvatarBadge.AutoDelete(seconds.shortTimerString())
    Subscription -> AvatarBadge.Subscription
}

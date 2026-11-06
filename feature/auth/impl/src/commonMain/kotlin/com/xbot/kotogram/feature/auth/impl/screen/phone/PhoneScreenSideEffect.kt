package com.xbot.kotogram.feature.auth.impl.screen.phone

internal sealed interface PhoneScreenSideEffect {
    data object Submitted : PhoneScreenSideEffect
}

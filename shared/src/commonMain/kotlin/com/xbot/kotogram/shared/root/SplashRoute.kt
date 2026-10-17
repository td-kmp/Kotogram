package com.xbot.kotogram.shared.root

import com.xbot.kotogram.core.navigation.NavKey
import kotlinx.serialization.Serializable

/** Shown until TDLib reports the first authorization state. */
@Serializable
internal data object SplashRoute : NavKey

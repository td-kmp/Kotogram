package com.xbot.kotogram.core.resources

import kotlinx.coroutines.flow.StateFlow

interface LocalizationSource {
    // Null until the chosen language is ready.
    val localization: StateFlow<Localization?>
}

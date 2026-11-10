package com.xbot.kotogram.core.resources

import kotlinx.coroutines.flow.StateFlow

interface LocalizationSource {
    val localization: StateFlow<Localization?>
}

package com.xbot.kotogram.core.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState

// TDLib is in a worker here: the bundled string shows until its answer, as Compose's own stringResource.
@Composable
internal actual fun rememberLanguagePackString(
    pack: LanguagePack,
    key: String,
    revision: Int
): String? {
    val value by produceState<String?>(null, pack, key, revision) { value = pack.string(key) }
    return value
}

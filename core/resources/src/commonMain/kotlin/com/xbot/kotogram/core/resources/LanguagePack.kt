package com.xbot.kotogram.core.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.StateFlow

// Telegram's language pack: null strings fall back to the bundled ones.
interface LanguagePack {
    // Changes when the pack's strings do, so screens read them again.
    val revision: StateFlow<Int>

    suspend fun string(key: String): String?
}

val LocalLanguagePack = staticCompositionLocalOf<LanguagePack?> { null }

@Composable
internal fun languagePackString(resourceKey: String): String? {
    val pack = LocalLanguagePack.current
    val key = LanguagePackKeys[resourceKey]
    if (pack == null || key == null) return null
    val revision by pack.revision.collectAsState()
    return rememberLanguagePackString(pack, key, revision)
}

@Composable
internal expect fun rememberLanguagePackString(
    pack: LanguagePack,
    key: String,
    revision: Int
): String?

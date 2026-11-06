package com.xbot.kotogram.core.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.runBlocking

// As Compose's own stringResource here: the first frame waits, and TDLib reads its database at once.
@Composable
internal actual fun rememberLanguagePackString(
    pack: LanguagePack,
    key: String,
    revision: Int
): String? = remember(pack, key, revision) { runBlocking { pack.string(key) } }

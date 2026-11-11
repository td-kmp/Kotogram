package com.xbot.kotogram.core.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import io.xbot.tdlib.api.LanguagePackInfo
import io.xbot.tdlib.api.LanguagePackStringValue

@Immutable
interface Localization {
    val language: LanguagePackInfo?

    fun string(key: String): LanguagePackStringValue?
}

object BundledLocalization : Localization {
    override val language: LanguagePackInfo? = null

    override fun string(key: String): LanguagePackStringValue? = null
}

val LocalLocalization = staticCompositionLocalOf<Localization> { BundledLocalization }

@Composable
fun ProvideLocalization(
    localization: Localization,
    content: @Composable () -> Unit,
) {
    val layoutDirection = if (localization.language?.isRtl == true) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(
        LocalLocalization provides localization,
        LocalLayoutDirection provides layoutDirection,
        bundledComposeEnvironment(),
        content = content,
    )
}

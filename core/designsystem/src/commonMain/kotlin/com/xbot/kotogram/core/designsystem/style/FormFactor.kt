package com.xbot.kotogram.core.designsystem.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.xbot.kotogram.core.designsystem.media.LocalUiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.KeyboardKind
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PointerPrecision

// For previews and the catalog: a laptop or a touch screen, whatever the device; the window stays the device's.
@Composable
fun ProvideFormFactor(laptop: Boolean, content: @Composable () -> Unit) {
    val mediaScope = LocalUiMediaScope.current
    val formFactorScope = remember(mediaScope, laptop) { FormFactorUiMediaScope(mediaScope, laptop) }
    CompositionLocalProvider(
        LocalUiMediaScope provides formFactorScope,
        LocalMediaQueryInfo provides if (laptop) LaptopMediaQueryInfo else TouchMediaQueryInfo,
        content = content
    )
}

private class FormFactorUiMediaScope(mediaScope: UiMediaScope, laptop: Boolean) : UiMediaScope by mediaScope {
    override val pointerPrecision: PointerPrecision = if (laptop) PointerPrecision.Fine else PointerPrecision.Coarse

    override val keyboardKind: KeyboardKind = if (laptop) KeyboardKind.Physical else KeyboardKind.Virtual
}

private val LaptopMediaQueryInfo = MediaQueryInfo(isLaptop = true, isTv = false, isAuto = false)

private val TouchMediaQueryInfo = MediaQueryInfo(isLaptop = false, isTv = false, isAuto = false)

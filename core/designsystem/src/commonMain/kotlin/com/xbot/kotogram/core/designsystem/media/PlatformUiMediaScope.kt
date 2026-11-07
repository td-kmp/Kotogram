package com.xbot.kotogram.core.designsystem.media

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.Dp
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.KeyboardKind
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PlatformFeature
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PointerPrecision
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.Posture
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.ViewingDistance

@Stable
internal class PlatformUiMediaScope(
    private val windowInfo: WindowInfo,
    pointerPrecision: PointerPrecision,
    keyboardKind: KeyboardKind,
    override val platformFeatures: Set<PlatformFeature>
) : UiMediaScope {
    override var pointerPrecision by mutableStateOf(pointerPrecision)

    override var keyboardKind by mutableStateOf(keyboardKind)

    override val windowPosture: Posture get() = Posture.Flat

    override val windowWidth: Dp get() = windowInfo.containerDpSize.width

    override val windowHeight: Dp get() = windowInfo.containerDpSize.height

    override val hasMicrophone: Boolean get() = PlatformFeature.Microphone in platformFeatures

    override val hasCamera: Boolean get() = PlatformFeature.Camera in platformFeatures

    override val viewingDistance: ViewingDistance get() = ViewingDistance.Near
}

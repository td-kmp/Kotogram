package com.xbot.kotogram.feature.auth.impl.screen.phone

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.media.LocalUiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.FoldOrientation
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.FoldOrientation.Companion.Horizontal
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.FoldOrientation.Companion.Vertical
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.FoldState
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.KeyboardKind
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PointerPrecision
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.ViewingDistance
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.WindowFold
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.WindowPosture
import com.xbot.kotogram.core.designsystem.style.LocalMediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import io.xbot.tdlib.api.CountryInfo
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

@RunWith(Parameterized::class)
class PhoneScreenScreenshotTest(
    private val device: Device,
    private val darkTheme: Boolean,
) {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun screenshot() = runDesktopComposeUiTest(
        width = (device.width.value * device.scale).toInt(),
        height = (device.height.value * device.scale).toInt(),
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(device.scale)) {
                KotogramTheme(darkTheme = darkTheme, dynamicColor = false) {
                    CompositionLocalProvider(
                        LocalUiMediaScope provides device.mediaScope,
                        LocalMediaQueryInfo provides device.mediaQueryInfo,
                    ) {
                        PhoneScreenContent(
                            state = entered,
                            phoneNumber = TextFieldState(entered.callingCode + entered.number),
                            canUseQr = device.laptop,
                            onAction = {},
                            onQrLoginClick = {},
                        )
                    }
                }
            }
        }
        waitForIdle()
        val file = File("build/screenshots/phone/${device.id}_${if (darkTheme) "dark" else "light"}.png")
        file.parentFile.mkdirs()
        ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", file)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}, dark {1}")
        fun devices(): List<Array<Any>> =
            Device.entries.map { arrayOf<Any>(it, true) } + listOf(arrayOf<Any>(Device.IPhoneAir, false))
    }
}

enum class Device(
    val id: String,
    val width: Dp,
    val height: Dp,
    val scale: Float,
    val laptop: Boolean,
    val posture: WindowPosture = WindowPosture(emptyList()),
) {
    IPhoneAir("iphone-air", 420.dp, 912.dp, 3f, laptop = false),
    IPhoneAirLandscape("iphone-air-landscape", 912.dp, 420.dp, 2f, laptop = false),
    IPadPro11("ipad-pro-11", 834.dp, 1210.dp, 2f, laptop = false),
    FoldableUnfolded("foldable-unfolded", 673.dp, 841.dp, 2f, laptop = false),
    FoldableTabletop("foldable-tabletop", 841.dp, 673.dp, 2f, laptop = false, posture = halfOpened(Horizontal)),
    FoldableBook("foldable-book", 841.dp, 673.dp, 2f, laptop = false, posture = halfOpened(Vertical)),
    Desktop("desktop", 700.dp, 550.dp, 2f, laptop = true),
    ;

    val mediaScope: UiMediaScope = object : UiMediaScope {
        override val windowPosture = posture
        override val windowWidth = width
        override val windowHeight = height
        override val pointerPrecision = if (laptop) PointerPrecision.Fine else PointerPrecision.Coarse
        override val keyboardKind = if (laptop) KeyboardKind.Physical else KeyboardKind.Virtual
        override val hasMicrophone = false
        override val hasCamera = false
        override val viewingDistance = ViewingDistance.Near
    }

    val mediaQueryInfo = MediaQueryInfo(isLaptop = laptop, isTv = false, isAuto = false)
}

private fun halfOpened(orientation: FoldOrientation) =
    WindowPosture(listOf(WindowFold(FoldState.HalfOpened, orientation)))

private val uzbekistan = CountryInfo(
    countryCode = "UZ",
    name = "Uzbekistan",
    englishName = "Uzbekistan",
    flagEmoji = "🇺🇿",
    isHidden = false,
    callingCodes = listOf("998"),
)

private val entered = PhoneScreenState(
    countries = listOf(uzbekistan),
    country = uzbekistan,
    callingCode = "998",
    number = "933988",
    numberPattern = "93 398 8- --",
    isAcceptingNumber = true,
)

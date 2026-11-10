package com.xbot.kotogram.feature.auth.impl.screen.qr

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.media.LocalUiMediaScope
import com.xbot.kotogram.core.designsystem.style.LocalMediaQueryInfo
import com.xbot.kotogram.feature.auth.impl.screen.phone.Device
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

@RunWith(Parameterized::class)
class QrScreenScreenshotTest(
    private val device: Device,
) {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun screenshot() = runDesktopComposeUiTest(
        width = (device.width.value * device.scale).toInt(),
        height = (device.height.value * device.scale).toInt(),
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(device.scale)) {
                KotogramTheme(darkTheme = true, dynamicColor = false) {
                    CompositionLocalProvider(
                        LocalUiMediaScope provides device.mediaScope,
                        LocalMediaQueryInfo provides device.mediaQueryInfo,
                    ) {
                        QrScreenContent(state = QrScreenState(link = LINK), onPhoneLoginClick = {})
                    }
                }
            }
        }
        mainClock.advanceTimeBy(SETTLE_MILLIS)
        val file = File("build/screenshots/qr/${device.id}.png")
        file.parentFile.mkdirs()
        ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", file)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun devices(): List<Device> = Device.entries
    }
}

private const val LINK = "tg://login?token=AQFJjO1qHocdoolqLtckd6dPS98k1HhnVDFilU_N_v6psw"
private const val SETTLE_MILLIS = 6_000L

package com.xbot.kotogram.feature.auth.impl.screen.code

import androidx.compose.foundation.text.input.TextFieldState
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
import io.xbot.tdlib.api.AuthenticationCodeInfo
import io.xbot.tdlib.api.AuthenticationCodeTypeSms
import io.xbot.tdlib.api.AuthenticationCodeTypeTelegramMessage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CodeScreenScreenshotTest(private val device: Device, private val darkTheme: Boolean) {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun screenshot() = runDesktopComposeUiTest(
        width = (device.width.value * device.scale).toInt(),
        height = (device.height.value * device.scale).toInt()
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(device.scale)) {
                KotogramTheme(darkTheme = darkTheme, dynamicColor = false) {
                    CompositionLocalProvider(
                        LocalUiMediaScope provides device.mediaScope,
                        LocalMediaQueryInfo provides device.mediaQueryInfo
                    ) {
                        CodeScreenContent(
                            state = waiting,
                            code = TextFieldState("12"),
                            onAction = {},
                            onChangeNumberClick = {}
                        )
                    }
                }
            }
        }
        waitForIdle()
        val file =
            File("build/screenshots/code/${device.id}_${if (darkTheme) "dark" else "light"}.png")
        file.parentFile.mkdirs()
        ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", file)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}, dark {1}")
        fun devices(): List<Array<Any>> = Device.entries.map { arrayOf<Any>(it, true) } +
            listOf(arrayOf<Any>(Device.IPhoneAir, false))
    }
}

private val waiting = CodeScreenState(
    info = AuthenticationCodeInfo(
        phoneNumber = "+998 93 398 87 65",
        type = AuthenticationCodeTypeTelegramMessage(length = 5),
        nextType = AuthenticationCodeTypeSms(length = 5),
        timeout = 60
    ),
    resendSecondsLeft = 45
)

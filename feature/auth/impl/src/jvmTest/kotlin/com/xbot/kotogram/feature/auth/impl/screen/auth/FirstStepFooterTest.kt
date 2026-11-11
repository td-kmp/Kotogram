package com.xbot.kotogram.feature.auth.impl.screen.auth

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.media.LocalUiMediaScope
import com.xbot.kotogram.core.designsystem.style.LocalMediaQueryInfo
import com.xbot.kotogram.feature.auth.impl.screen.phone.Device
import com.xbot.kotogram.feature.auth.impl.screen.phone.PhoneScreenContent
import com.xbot.kotogram.feature.auth.impl.screen.phone.PhoneScreenState
import com.xbot.kotogram.feature.auth.impl.screen.qr.QrScreenContent
import com.xbot.kotogram.feature.auth.impl.screen.qr.QrScreenState
import com.xbot.kotogram.feature.language.domain.model.LanguageSuggestion
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class FirstStepFooterTest {
    @Test
    fun theSuggestionStandsUnderThePhoneScreensAction() = onPhone {
        setScreen { PhoneScreen(footer = { Suggestion() }) }
        val action = onNodeWithText(NEXT).getUnclippedBoundsInRoot()
        val link = onNodeWithText(SUGGESTION).getUnclippedBoundsInRoot()
        assertNear(action.bottom, link.top)
        assertNear(PHONE.height - LINK_BOTTOM, link.bottom)
    }

    @Test
    fun thePhoneScreensActionKeepsItsInsetWithoutASuggestion() = onPhone {
        setScreen { PhoneScreen(footer = {}) }
        assertNear(PHONE.height - ACTION_INSET, onNodeWithText(NEXT).getUnclippedBoundsInRoot().bottom)
    }

    @Test
    fun theSuggestionStandsUnderTheQrScreensAction() = onPhone {
        setScreen {
            QrScreenContent(state = QrScreenState(link = QR_LINK), onPhoneLoginClick = {}, footer = { Suggestion() })
        }
        val action = onNodeWithText(PHONE_LOGIN).getUnclippedBoundsInRoot()
        val link = onNodeWithText(SUGGESTION).getUnclippedBoundsInRoot()
        assertNear(action.bottom, link.top)
        assertNear(PHONE.height - LINK_BOTTOM, link.bottom)
    }

    @Test
    fun theQrScreensActionKeepsItsInsetWithoutASuggestion() = onPhone {
        setScreen { QrScreenContent(state = QrScreenState(link = QR_LINK), onPhoneLoginClick = {}) }
        assertNear(PHONE.height - ACTION_INSET, onNodeWithText(PHONE_LOGIN).getUnclippedBoundsInRoot().bottom)
    }

    private fun onPhone(block: DesktopComposeUiTest.() -> Unit) = runDesktopComposeUiTest(
        width = (PHONE.width.value * PHONE.scale).toInt(),
        height = (PHONE.height.value * PHONE.scale).toInt(),
    ) { block() }

    private fun DesktopComposeUiTest.setScreen(screen: @Composable () -> Unit) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(PHONE.scale)) {
                KotogramTheme(darkTheme = true, dynamicColor = false) {
                    CompositionLocalProvider(
                        LocalUiMediaScope provides PHONE.mediaScope,
                        LocalMediaQueryInfo provides PHONE.mediaQueryInfo,
                        content = screen,
                    )
                }
            }
        }
        waitForIdle()
    }

    private fun assertNear(
        expected: Dp,
        actual: Dp,
    ) = assertEquals(expected.value, actual.value, absoluteTolerance = 0.5f)
}

@Composable
private fun PhoneScreen(footer: @Composable () -> Unit) {
    PhoneScreenContent(
        state = PhoneScreenState(callingCode = "998", number = "901234567", isAcceptingNumber = true),
        phoneNumber = TextFieldState("998901234567"),
        canUseQr = false,
        onAction = {},
        onQrLoginClick = {},
        footer = footer,
    )
}

@Composable
private fun Suggestion() {
    LanguageSuggestionContent(
        state = LanguageSuggestionState(suggestion = LanguageSuggestion(languagePackId = "ru", text = SUGGESTION)),
        onAction = {},
    )
}

private val PHONE = Device.IPhoneAir
private val ACTION_INSET = 24.dp
private val LINK_BOTTOM = 8.dp
private const val NEXT = "Next"
private const val PHONE_LOGIN = "Log in by phone number"
private const val SUGGESTION = "Продолжить на русском"
private const val QR_LINK = "tg://login?token=AQIDBAUGBwgJCgsMDQ4PEA"

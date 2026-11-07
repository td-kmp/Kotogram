package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CodeFieldTest {
    @Test
    fun keepsOnlyDigits() = withCodeField(length = 5) { state, field ->
        field.performTextInput("1a2-3")
        assertEquals("123", state.text.toString())
    }

    @Test
    fun stopsAtTheCodesLength() = withCodeField(length = 5) { state, field ->
        field.performTextInput("1234")
        field.performTextInput("56")
        assertEquals("1234", state.text.toString())
        field.performTextInput("5")
        field.performTextInput("6")
        assertEquals("12345", state.text.toString())
    }

    @Test
    fun aPastedCodeReplacesAFullField() = withCodeField(length = 5, text = "11111") { state, field ->
        field.performTextInput("22 222")
        assertEquals("22222", state.text.toString())
    }

    @Test
    fun theCursorStaysAtTheEnd() = withCodeField(length = 5, text = "123") { state, field ->
        field.performTextInputSelection(TextRange(0, 2))
        waitForIdle()
        assertEquals(TextRange(3), state.selection)
        field.performTextInput("4")
        assertEquals("1234", state.text.toString())
    }

    @Test
    fun cellsGrowWithTheFontScale() = withCodeField(length = 5, fontScale = 2f) { _, field ->
        // 24 sp at twice the scale is 48 dp, and a cell is 40 / 24 of its digit, as on macOS.
        field.assertHeightIsEqualTo(80.dp)
    }

    @Test
    fun cellsNarrowToFitTheWidth() = withCodeField(length = 6, fontScale = 2f, width = 280.dp) { _, field ->
        field.assertWidthIsEqualTo(280.dp)
        field.assertHeightIsEqualTo(80.dp)
    }

    private fun withCodeField(
        length: Int,
        text: String = "",
        fontScale: Float = 1f,
        width: Dp = Dp.Unspecified,
        block: ComposeUiTest.(TextFieldState, SemanticsNodeInteraction) -> Unit
    ) = runComposeUiTest {
        val state = TextFieldState(text)
        setContent {
            val density = Density(LocalDensity.current.density, fontScale)
            CompositionLocalProvider(LocalDensity provides density) {
                KotogramTheme(darkTheme = false, dynamicColor = false) {
                    Box(if (width == Dp.Unspecified) Modifier else Modifier.width(width)) {
                        CodeField(state = state, length = length)
                    }
                }
            }
        }
        block(state, onNode(hasSetTextAction()))
    }
}
